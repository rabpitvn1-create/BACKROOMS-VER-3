package com.rabpit.backroom.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class GameCoreFacade private constructor(
  private val repository: SaveRepository,
  private val logger: GamePipelineLogger
) : AutoCloseable {
  private val rules = RuleIntentInterpreter()
  private val resolver = CommandResolver()

  /** Fast deterministic pass. Gemini is never called from this method. */
  fun processRule(legacyStateJson: String, action: String): String {
    val legacy = JSONObject(legacyStateJson)
    val state = loadOrMigrate(legacy)
    val turnId = nextTurnId(legacy, state)
    logger.log(PipelineLogEvent("INPUT", turnId = turnId, details = mapOf("length" to action.length.toString())))
    val pending = TurnCoordinator.createPending(state, turnId, action)
    if (pending.error != null) return response(false, legacy, pending.error, "pending_rejected")
    val context = contextFor(pending.state)
    val ruleResult = rules.interpretSync(action, context)
    val interpreted = IntentResult(
      ruleResult.candidates,
      ruleResult.candidates.any { it.confidence != IntentConfidence.HIGH && it.intent != GameIntent.NO_ACTION }
    )
    interpreted.candidates.forEach { logger.log(PipelineLogEvent("INTENT", turnId = turnId, source = it.source, intent = it.intent, confidence = it.score)) }

    // Player text never has authority to manufacture an acquisition event. Reject immediately,
    // do not call Gemini, do not advance the turn, and do not mutate Inventory.
    if (isDirectPlayerPickupAction(action) || interpreted.candidates.any { it.intent == GameIntent.PICKUP_ITEM }) {
      val result = syncLegacy(legacy, state, incrementTurn = false)
      val reply = validationReply("player_pickup_unavailable")
      appendLog(result, action, reply)
      logger.log(PipelineLogEvent("REJECT", turnId = turnId, details = mapOf("reason" to "player_pickup_unavailable")))
      return response(true, result, "player_pickup_unavailable", "validation_rejected", reply)
    }

    // Restore is lore/narrative-only. Route prose to the GM, but authoritative state mutation is
    // explicitly suppressed again in processValidatedCandidate().
    if (interpreted.candidates.any { it.intent == GameIntent.OMNIVAULT_RESTORE }) {
      return response(false, legacy, null, "fallback_required")
    }

    if (interpreted.candidates.any { it.intent == GameIntent.NO_ACTION || it.confidence != IntentConfidence.HIGH }) {
      return response(false, legacy, null, "fallback_required")
    }
    val resolvedCommands = interpreted.candidates.mapIndexedNotNull { index, candidate -> resolver.resolve(candidate, index, turnId, context) }
    if (resolvedCommands.size != interpreted.candidates.size || resolvedCommands.isEmpty()) return response(false, legacy, null, "resolution_incomplete")
    val commands = resolvedCommands.toMutableList()
    commands += timeAdvanceCommand(turnId, action)
    commands.forEach { logger.log(PipelineLogEvent("COMMAND", turnId, it.commandId, it.source)) }
    val committed = TurnCoordinator.commit(pending.state, commands)
    if (committed.error != null) {
      val rejected = TurnCoordinator.reject(pending.state, committed.error)
      repository.save(rejected.state)
      val result = syncLegacy(legacy, rejected.state, incrementTurn = true)
      appendLog(result, action, validationReply(committed.error))
      return response(true, result, committed.error, "validation_rejected", validationReply(committed.error))
    }
    repository.save(committed.state)
    val result = syncLegacy(legacy, committed.state, incrementTurn = true)
    val reply = eventReply(committed.execution?.events.orEmpty())
    appendLog(result, action, reply)
    logger.log(PipelineLogEvent("COMMIT", turnId = turnId, details = mapOf("commands" to commands.size.toString())))
    return response(true, result, null, "committed", reply)
  }

  fun currentCoreState(): String = GameStateCodec.encode(repository.load())

  fun processItemAction(
    stateJson: String,
    ownerId: String,
    itemId: String,
    operation: String,
    targetId: String,
    quantity: Int
  ): String {
    val legacy = JSONObject(stateJson)
    val state = loadOrMigrate(legacy)
    if (CombatRuntime.active(state) != null) {
      return response(false, syncLegacy(legacy, state, false), "combat_locked", "item_action_rejected")
    }
    val actorId = resolveCharacterId(state, ownerId) ?: state.party.leaderId
    val inventory = state.inventories[actorId] ?: InventoryState(actorId)
    val stack = inventory.items[itemId]
      ?: inventory.items.values.firstOrNull { it.itemId == itemId || it.archetypeId == itemId || it.name.equals(itemId, true) }
      ?: return response(false, syncLegacy(legacy, state, false), "item_not_owned", "item_action_rejected")
    val op = operation.trim().lowercase()
    val target = when (op) {
      "share" -> resolveCharacterId(state, targetId)
        ?: return response(false, syncLegacy(legacy, state, false), "target_unknown", "item_action_rejected")
      "use" -> actorId
      else -> null
    }
    val commandOperation = when (op) {
      "use", "share" -> ItemCommand.Operation.USE
      "discard", "drop" -> ItemCommand.Operation.DROP
      else -> return response(false, syncLegacy(legacy, state, false), "item_action_invalid", "item_action_rejected")
    }
    val command = ItemCommand(
      commandId = nextUiCommandId(state, "ITEM"),
      turnId = state.turn.currentTurnId,
      actorId = actorId,
      targetId = target,
      source = CommandSource.UI,
      operation = commandOperation,
      itemId = stack.itemId,
      itemName = stack.name,
      quantity = quantity.coerceAtLeast(1).coerceAtMost(stack.quantity)
    )
    val execution = StateReducer.execute(state, command)
    if (!execution.applied) {
      val reason = execution.validation.reason ?: "item_action_rejected"
      return response(false, syncLegacy(legacy, state, false), reason, "item_action_rejected")
    }
    repository.save(execution.state)
    return response(true, syncLegacy(legacy, execution.state, false), null, "item_action_committed", eventReply(execution.events))
  }

  fun processCoreUpgrade(stateJson: String, characterId: String, stat: String): String {
    val legacy = JSONObject(stateJson)
    val state = loadOrMigrate(legacy)
    val targetId = resolveCharacterId(state, characterId)
      ?: return response(false, syncLegacy(legacy, state, false), "target_unknown", "core_upgrade_rejected")
    val execution = StateReducer.execute(state, StatUpgradeCommand(
      commandId = nextUiCommandId(state, "STAT"),
      turnId = state.turn.currentTurnId,
      actorId = state.party.leaderId,
      targetId = targetId,
      source = CommandSource.UI,
      stat = stat
    ))
    if (!execution.applied) {
      val reason = execution.validation.reason ?: "core_upgrade_rejected"
      return response(false, syncLegacy(legacy, state, false), reason, "core_upgrade_rejected")
    }
    repository.save(execution.state)
    return response(true, syncLegacy(legacy, execution.state, false), null, "core_upgrade_committed", "Đã nâng chỉ số.")
  }

  fun combatState(stateJson: String): String {
    val legacy = JSONObject(stateJson)
    val state = loadOrMigrate(legacy)
    return JSONObject().apply {
      put("handled", CombatRuntime.active(state) != null)
      put("state", syncLegacy(legacy, state, false))
      CombatRuntime.toJson(state)?.let { put("combat", it) }
    }.toString()
  }

  fun combatHold(stateJson: String, dieIndex: Int, held: Boolean): String =
    mutateCombat(stateJson) { CombatRuntime.setHold(it, dieIndex, held) }

  fun combatRoll(stateJson: String): String =
    mutateCombat(stateJson, CombatRuntime::rerollDice)

  fun combatFinish(stateJson: String): String =
    mutateCombat(stateJson, CombatRuntime::finishHand)

  fun combatResolve(stateJson: String): String {
    val legacy = JSONObject(stateJson)
    val state = loadOrMigrate(legacy)
    return try {
      val resolution = CombatRuntime.resolveFinalizedHand(state)
      repository.save(resolution.state)
      JSONObject().apply {
        put("handled", resolution.handled)
        put("state", syncLegacy(legacy, resolution.state, false))
        put("reply", resolution.reply)
        CombatRuntime.toJson(resolution.state)?.let { put("combat", it) }
      }.toString()
    } catch (error: Exception) {
      response(false, syncLegacy(legacy, state, false), error.message ?: "combat_resolve_failed", "combat_resolve_rejected")
    }
  }

  private fun mutateCombat(stateJson: String, mutation: (GameState) -> GameState): String {
    val legacy = JSONObject(stateJson)
    val state = loadOrMigrate(legacy)
    return try {
      val next = mutation(state)
      repository.save(next)
      JSONObject().apply {
        put("handled", true)
        put("state", syncLegacy(legacy, next, false))
        CombatRuntime.toJson(next)?.let { put("combat", it) }
      }.toString()
    } catch (error: Exception) {
      response(false, syncLegacy(legacy, state, false), error.message ?: "combat_action_failed", "combat_action_rejected")
    }
  }

  fun clear() = repository.clear()
  override fun close() = Unit

  /**
   * Commits only the gameplay delta already accepted by the legacy canon/dice validator.
   * Candidate prose/JSON never becomes storage directly: inventory and party are rebuilt
   * from commands and then projected back onto the UI state.
   */
  fun processValidatedCandidate(beforeJson: String, candidateJson: String, action: String): String {
    val before = JSONObject(beforeJson)
    val candidate = JSONObject(candidateJson)
    val core = loadOrMigrate(before)
    val turnId = nextTurnId(before, core)
    val pending = TurnCoordinator.createPending(core, turnId, action)
    if (pending.error != null) return response(false, before, pending.error, "pending_rejected")
    val commands = mutableListOf<GameCommand>()
    val current = pending.state.inventories[KAI_ID]?.items.orEmpty()
    val actionIntents = rules.interpretSync(action, contextFor(pending.state)).candidates.map { it.intent }.toSet()
    val inventoryLocked = isDirectPlayerPickupAction(action) || GameIntent.PICKUP_ITEM in actionIntents || GameIntent.OMNIVAULT_RESTORE in actionIntents

    val desiredById = mutableMapOf<String, ItemStack>()
    if (inventoryLocked) {
      desiredById.putAll(current)
    } else {
      val desiredInventory = candidate.optJSONArray("inventory") ?: JSONArray()
      for (index in 0 until desiredInventory.length()) {
        val json = desiredInventory.optJSONObject(index) ?: continue
        val name = json.optString("name").trim(); if (name.isEmpty()) continue
        val id = json.optString("id").ifBlank { stableItemId(name) }
        val currentStack = current[id]
        val metadata = currentStack?.metadata.orEmpty() + jsonObjectStrings(json.optJSONObject("metadata"))
        desiredById[id] = ItemStack(
          id,
          name,
          json.optInt("quantity", 1).coerceAtLeast(1),
          json.optString("state").takeIf(String::isNotBlank) ?: currentStack?.condition,
          metadata,
          currentStack?.archetypeId ?: id,
          currentStack?.contentState ?: ContentState.NONE
        )
      }
    }

    (current.keys + desiredById.keys).sorted().forEachIndexed { index, id ->
      val old = current[id]?.quantity ?: 0; val desired = desiredById[id]?.quantity ?: 0
      if (desired == old) return@forEachIndexed
      val stack = desiredById[id] ?: current.getValue(id)
      commands += ItemCommand(
        "$turnId:GEMINI:INV:$index", turnId, KAI_ID, source = CommandSource.GEMINI,
        operation = if (desired > old) ItemCommand.Operation.PICKUP else ItemCommand.Operation.DROP,
        itemId = id, itemName = stack.name, quantity = kotlin.math.abs(desired - old), metadata = stack.metadata
      )
    }

    val desiredParty = mutableMapOf<String, JSONObject>()
    val partyJson = candidate.optJSONArray("party") ?: JSONArray()
    for (index in 0 until partyJson.length()) {
      val member = partyJson.optJSONObject(index) ?: continue
      val id = member.optString("id").ifBlank { member.optString("name").trim().lowercase() }
      if (id.isNotBlank()) desiredParty[id] = member
    }
    val currentFollowers = pending.state.party.memberIds.filter { it != KAI_ID }.toSet()
    (currentFollowers - desiredParty.keys).sorted().forEachIndexed { index, id ->
      commands += PartyCommand("$turnId:GEMINI:PARTY_REMOVE:$index", turnId, KAI_ID, id, CommandSource.GEMINI, PartyCommand.Operation.REMOVE)
    }
    (desiredParty.keys - currentFollowers).sorted().forEachIndexed { index, id ->
      val member = desiredParty.getValue(id)
      val known = pending.state.characters[id]
      commands += PartyCommand(
        "$turnId:GEMINI:PARTY_ADD:$index", turnId, KAI_ID, id, CommandSource.GEMINI, PartyCommand.Operation.ADD,
        consentConfirmed = member.optBoolean("joinConfirmed", false) && known?.metadata?.get("joinEligible") == "true",
        targetPresent = member.optBoolean("present", false) && known?.presence == CharacterPresence.ACTIVE
      )
    }
    commands += ValidatedLegacyStateCommand(
      commandId = "$turnId:GEMINI:VALIDATED_STATE", turnId = turnId, source = CommandSource.GEMINI,
      location = candidate.optString("location").takeIf(String::isNotBlank),
      title = candidate.optString("title").takeIf(String::isNotBlank),
      levelJson = candidate.optJSONObject("level")?.toString(),
      playerJson = candidate.optJSONObject("player")?.toString(),
      flagsJson = candidate.optJSONObject("flags")?.toString(),
      validatedByGameEngine = true
    )
    commands += timeAdvanceCommand(turnId, action)

    val committed = TurnCoordinator.commit(pending.state, commands)
    if (committed.error != null) {
      logger.log(PipelineLogEvent("GEMINI_REJECTED", turnId = turnId, source = CommandSource.GEMINI, details = mapOf("reason" to committed.error)))
      return response(false, before, committed.error, "gemini_delta_rejected")
    }
    repository.save(committed.state)
    val synchronized = syncLegacy(candidate, committed.state, incrementTurn = false)
    logger.log(PipelineLogEvent("GEMINI_COMMIT", turnId = turnId, source = CommandSource.GEMINI, details = mapOf("commands" to commands.size.toString(), "inventoryLocked" to inventoryLocked.toString())))
    return response(true, synchronized, null, "gemini_delta_committed")
  }

  private fun loadOrMigrate(legacy: JSONObject): GameState {
    if (repository.exists()) return repository.load()
    val migrated = GameStateCodec.decode(legacy)
    repository.save(migrated)
    return migrated
  }

  private fun contextFor(state: GameState): GameContext {
    val actors = state.characters.values.associate { it.name.lowercase() to it.id } + mapOf("kai" to KAI_ID, "iris" to "iris", "syvial" to "syvial")
    val items = (state.inventories.values.flatMap { it.items.values } + state.omnivault.storedItems.values).associate { it.name.lowercase() to it.itemId }
    return GameContext(state, actors, items)
  }

  private fun isDirectPlayerPickupAction(action: String): Boolean {
    val text = action.trim()
    val omnivaultWithdrawal = Regex("(?:lấy|rút|triệu hồi).*(?:ra khỏi|khỏi|từ).*(?:omnivault|nhẫn|kho)", RegexOption.IGNORE_CASE).containsMatchIn(text)
    if (omnivaultWithdrawal) return false
    val directVerb = Regex("(?:^|\\s)(?:nhặt|lượm|cầm\\s+lên|lấy(?:\\s+lên)?|thu\\s+hồi|tịch\\s+thu|nhận(?:\\s+lấy)?|pick\\s+up|take|receive)(?:\\s|$)", RegexOption.IGNORE_CASE)
    val inventoryAssertion = Regex("(?:thêm|bỏ|đưa).{0,80}(?:vào|trong)\\s+(?:inventory|kho đồ|túi đồ)", RegexOption.IGNORE_CASE)
    return directVerb.containsMatchIn(text) || inventoryAssertion.containsMatchIn(text)
  }

  private fun nextTurnId(legacy: JSONObject, state: GameState): String {
    val number = legacy.optInt("turn", state.turn.currentTurnId.substringAfterLast('_').toIntOrNull() ?: 1)
    return "TURN_${number.coerceAtLeast(1)}"
  }

  private fun timeAdvanceCommand(turnId: String, action: String): TimeAdvanceCommand = TimeAdvanceCommand(
    commandId = "$turnId:SYSTEM:TIME",
    turnId = turnId,
    actorId = KAI_ID,
    source = CommandSource.SYSTEM,
    minutes = TimeCostPolicy.estimateMinutes(action),
    reason = "player_action"
  )

  private fun resolveCharacterId(state: GameState, raw: String?): String? {
    val value = raw.orEmpty().trim()
    if (value.isBlank()) return null
    state.characters[value]?.let { return it.id }
    val normalized = value.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_')
    if (normalized in setOf("cao_minh", "kai", "twilight")) return state.party.leaderId
    return state.characters.values.firstOrNull { character ->
      character.id.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_') == normalized ||
        character.name.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_') == normalized
    }?.id
  }

  private fun nextUiCommandId(state: GameState, kind: String): String {
    var sequence = state.turn.executedCommandIds.size
    var id: String
    do {
      id = "${state.turn.currentTurnId}:UI:$kind:${sequence++}"
    } while (id in state.turn.executedCommandIds)
    return id
  }

  private fun stableItemId(name: String): String = name.lowercase()
    .replace(Regex("[^\\p{L}\\p{N}]+"), "-").trim('-').ifBlank { "item-${name.hashCode().toUInt()}" }

  private fun jsonObjectStrings(json: JSONObject?): Map<String, String> {
    if (json == null) return emptyMap()
    val result = mutableMapOf<String, String>()
    json.keys().forEach { key -> result[key] = json.optString(key) }
    return result
  }

  private fun syncLegacy(legacy: JSONObject, state: GameState, incrementTurn: Boolean): JSONObject {
    val output = JSONObject(legacy.toString())
    if (incrementTurn) output.put("turn", output.optInt("turn", 1) + 1)
    output.put("saveVersion", CURRENT_SAVE_VERSION)
    output.put("gameTime", JSONObject().apply {
      put("elapsedSubjectiveMinutes", state.time.elapsedSubjectiveMinutes)
      put("lastAdvanceMinutes", state.time.lastAdvanceMinutes)
      state.time.lastAdvanceReason?.let { put("lastAdvanceReason", it) }
    })
    output.put("partyDetails", CharacterDetailJson.encodeParty(CharacterDetailProjector.projectParty(state)))
    output.put("coreResource", JSONObject().apply {
      put("quantity", state.coreResource.quantity)
      put("highestRewardedStageIndex", state.coreResource.highestRewardedStageIndex)
    })
    output.put("levelRuntime", JSONObject().apply {
      put("key", state.levelRuntime.key)
      put("stageIndex", state.levelRuntime.stageIndex)
      put("streak", state.levelRuntime.route.streak)
      put("exitAvailable", state.levelRuntime.route.exitAvailable)
    })
    CombatRuntime.toJson(state)?.let { output.put("combat", it) }
    val kaiInventory = state.inventories[KAI_ID]?.items?.values.orEmpty()
    output.put("inventory", JSONArray().apply { kaiInventory.forEach { stack -> put(JSONObject().apply {
      put("id", stack.itemId); put("name", stack.name); put("quantity", stack.quantity)
      stack.condition?.let { put("state", it) }; put("metadata", JSONObject(stack.metadata))
    }) } })
    output.put("party", JSONArray().apply { state.party.memberIds.filter { it != KAI_ID }.forEach { id ->
      state.characters[id]?.let { character -> put(JSONObject().apply {
        put("id", character.id); put("name", character.name); character.avatarRef?.let { put("avatar", it) }
        put("presence", character.presence.name)
      }) }
    } })
    state.world["location"]?.let { output.put("location", it) }
    state.world["title"]?.let { output.put("title", it) }
    state.world["levelJson"]?.let { output.put("level", JSONObject(it)) }
    state.world["flagsJson"]?.let { output.put("flags", JSONObject(it)) }
    state.metadata["legacyPlayerJson"]?.let { output.put("player", JSONObject(it)) }
    return output
  }

  private fun appendLog(state: JSONObject, action: String, reply: String) {
    val log = state.optJSONArray("log") ?: JSONArray().also { state.put("log", it) }
    log.put(JSONObject().put("role", "player").put("text", action))
    log.put(JSONObject().put("role", "gm").put("text", reply))
  }

  private fun response(handled: Boolean, state: JSONObject, error: String?, reason: String, reply: String? = null): String = JSONObject().apply {
    put("handled", handled); put("state", state); put("reason", reason)
    if (error != null) put("error", error); if (reply != null) put("reply", reply)
  }.toString()

  private fun eventReply(events: List<String>): String = when (events.lastOrNull { it != "time_advanced" }) {
    "inventory_pickup" -> "Inventory đã được cập nhật bởi một sự kiện vật phẩm hợp lệ."
    "inventory_remove" -> "Vật phẩm đã được loại khỏi Inventory theo hành động của Kai."
    "inventory_transfer" -> "Vật phẩm đã được chuyển giao."
    "item_equipped" -> "Vật phẩm đã được trang bị."
    "item_unequipped" -> "Vật phẩm đã được tháo khỏi trang bị."
    "item_consumed", "hp_restored", "physiology_food_restored", "physiology_water_restored" -> "Vật phẩm đã được sử dụng."
    "character_stat_upgraded" -> "Chỉ số nhân vật đã được nâng."
    "omnivault_stored" -> "Vật phẩm đã được cất vào Omnivault."
    "omnivault_withdrawn" -> "Vật phẩm đã được lấy ra khỏi Omnivault."
    "omnivault_scanned" -> "Omnivault đã ghi mẫu vào scan slot và đánh dấu bản gốc."
    "omnivault_copied" -> "Omnivault đã tạo bản sao từ mẫu còn hiệu lực."
    else -> "Hành động đã được Game State Core xác nhận."
  }

  private fun validationReply(reason: String): String {
    val message = when (reason) {
      "player_pickup_unavailable", "restore_narrative_only", "precise_content_amount_forbidden", "item_content_empty" -> "This action is not available."
      "scan_source_missing", "scan_template_missing" -> "There is no object available for scanning or multiplying."
      "insufficient_item_quantity", "item_not_owned" -> "This action is not available."
      "party_full" -> "Party đã đủ tối đa bốn thành viên."
      "join_not_confirmed" -> "Yêu cầu gia nhập chưa đủ điều kiện hoặc chưa được NPC xác nhận."
      "living_target_forbidden" -> "Omnivault không thể tác động lên sinh vật sống."
      else -> "This action is not available."
    }
    return "[Warning] $message"
  }

  companion object {
    @JvmStatic fun create(context: Context, debugLogging: Boolean = false): GameCoreFacade = GameCoreFacade(
      SharedPreferencesSaveRepository(context.applicationContext), AndroidGamePipelineLogger(debugLogging)
    )
  }
}
