package com.rabpit.backroom.core

import android.content.Context
import com.rabpit.backroom.core.gameplay.GameplayCatalog
import com.rabpit.backroom.core.gameplay.LevelGraph
import org.json.JSONArray
import org.json.JSONObject

class GameCoreFacade private constructor(
  private val repository: SaveRepository,
  private val logger: GamePipelineLogger,
  private val levelGraph: LevelGraph
) : AutoCloseable {
  private val rules = RuleIntentInterpreter()
  private val resolver = CommandResolver()

  /** Fast deterministic pass. Gemini is never called from this method. */
  fun processRule(uiStateJson: String, action: String): String {
    val ui = JSONObject(uiStateJson)
    val state = loadState(ui)
    val turnId = nextTurnId(ui, state)
    logger.log(PipelineLogEvent("INPUT", turnId = turnId, details = mapOf("length" to action.length.toString())))
    val pending = TurnCoordinator.createPending(state, turnId, action)
    if (pending.error != null) return response(false, ui, pending.error, "pending_rejected")
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
      val result = syncUiState(ui, state, incrementTurn = false)
      val reply = validationReply("player_pickup_unavailable")
      appendLog(result, action, reply)
      logger.log(PipelineLogEvent("REJECT", turnId = turnId, details = mapOf("reason" to "player_pickup_unavailable")))
      return response(true, result, "player_pickup_unavailable", "validation_rejected", reply)
    }

    if (isExplorerAction(action)) return processExplorerTurn(ui, pending.state, action, turnId)

    if (interpreted.candidates.any { it.intent == GameIntent.NO_ACTION || it.confidence != IntentConfidence.HIGH }) {
      return response(false, ui, null, "fallback_required")
    }
    val resolvedCommands = interpreted.candidates.mapIndexedNotNull { index, candidate -> resolver.resolve(candidate, index, turnId, context) }
    if (resolvedCommands.size != interpreted.candidates.size || resolvedCommands.isEmpty()) return response(false, ui, null, "resolution_incomplete")
    val commands = resolvedCommands.toMutableList()
    commands += timeAdvanceCommand(turnId, action)
    commands.forEach { logger.log(PipelineLogEvent("COMMAND", turnId, it.commandId, it.source)) }
    val committed = TurnCoordinator.commit(pending.state, commands)
    if (committed.error != null) {
      val rejected = TurnCoordinator.reject(pending.state, committed.error)
      repository.save(rejected.state)
      val result = syncUiState(ui, rejected.state, incrementTurn = true)
      appendLog(result, action, validationReply(committed.error))
      return response(true, result, committed.error, "validation_rejected", validationReply(committed.error))
    }
    repository.save(committed.state)
    val result = syncUiState(ui, committed.state, incrementTurn = true)
    val reply = eventReply(committed.execution?.events.orEmpty())
    appendLog(result, action, reply)
    logger.log(PipelineLogEvent("COMMIT", turnId = turnId, details = mapOf("commands" to commands.size.toString())))
    return response(true, result, null, "committed", reply)
  }

  fun normalizeState(stateJson: String): String {
    if (!repository.exists() && repository.hasCheckpoint()) {
      val (state, clientState) = repository.loadCheckpoint()
      return syncUiState(JSONObject(clientState), state, false).toString()
    }
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    return syncUiState(ui, state, false).toString()
  }

  fun startNewGame(initialJson: String): String {
    val ui = JSONObject(initialJson)
    val fresh = GameState.initial()
    repository.save(fresh)
    return syncUiState(ui, fresh, false).toString()
  }

  fun saveCheckpoint(stateJson: String): String {
    val ui = JSONObject(stateJson)
    if (!repository.exists()) repository.save(GameStateCodec.decode(ui))
    repository.saveCheckpoint(ui.toString())
    return ui.toString()
  }

  fun loadCheckpoint(): String {
    val (state, clientState) = repository.loadCheckpoint()
    return syncUiState(JSONObject(clientState), state, false).toString()
  }

  fun clearCheckpoint() = repository.clearCheckpoint()

  fun currentCoreState(): String = GameStateCodec.encode(repository.load())
  fun levelSnapshotDescriptor(turn: Int): String {
    val key = repository.load().levelRuntime.key
    return JSONObject().apply {
      put("levelKey", key)
      levelGraph.node(key)?.parentLevel?.let { put("level", it) }
      levelGraph.snapshotPath(key, turn)?.let { put("path", it) }
    }.toString()
  }

  fun processExplore(stateJson: String, action: String): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    val resolution = ExplorationRuntime.resolve(state, action, levelGraph)
    repository.save(resolution.state)
    return JSONObject().apply {
      put("handled", true)
      put("outcome", resolution.outcome.name)
      resolution.payloadKey?.let { put("payloadKey", it) }
      if (resolution.coreReward > 0) put("coreReward", resolution.coreReward)
      put("state", syncUiState(ui, resolution.state, false))
      CombatRuntime.toJson(resolution.state)?.let { put("combat", it) }
    }.toString()
  }

  private fun isExplorerAction(action: String): Boolean {
    val text = action.trim().lowercase()
    return text.startsWith("quan sát kỹ khu vực xung quanh") ||
      text.startsWith("kiểm tra các lối đi hoặc điểm bất thường") ||
      text.startsWith("tiếp tục khám phá") || text.startsWith("đi qua lối ra") ||
      text == "__loot:open_chest"
  }

  private fun processExplorerTurn(ui: JSONObject, pending: GameState, action: String, turnId: String): String {
    val resolution = if (action.trim() == "__loot:open_chest") ExplorationRuntime.openChest(pending)
      else ExplorationRuntime.resolve(pending, action, levelGraph)
    val explored = if (resolution.outcome == ExplorationOutcome.LEVEL_TRANSITION) {
      resolution.state.copy(world = resolution.state.world +
        ("location" to "Level ${resolution.state.levelRuntime.key}"))
    } else resolution.state
    val committed = TurnCoordinator.commit(explored, listOf(timeAdvanceCommand(turnId, action)))
    if (committed.error != null) {
      val rejected = TurnCoordinator.reject(pending, committed.error)
      repository.save(rejected.state)
      val result = syncUiState(ui, rejected.state, incrementTurn = true)
      appendLog(result, action, validationReply(committed.error))
      return response(true, result, committed.error, "validation_rejected")
    }
    repository.save(committed.state)
    val result = syncUiState(ui, committed.state, incrementTurn = true)
    val route = committed.state.levelRuntime.route
    val reply = when (resolution.outcome) {
      ExplorationOutcome.LEVEL_TRANSITION -> "Bạn đi qua lối ra và đến Level ${committed.state.levelRuntime.key}. Chuỗi khám phá tiếp tục ở chặng mới."
      ExplorationOutcome.EXIT_AVAILABLE -> "Bạn nhận ra lối ra của chặng này. Có thể đi qua để đến chặng kế tiếp."
      ExplorationOutcome.ROUTE_PROGRESS -> "Bạn lần theo dấu vết trong không gian. Tiến độ tìm lối ra: ${route.streak}/6."
      ExplorationOutcome.ROUTE_RESET -> "Lối đi vòng lại điểm ban đầu. Tiến độ tìm lối ra bắt đầu lại."
      ExplorationOutcome.CHEST -> if (action.trim() == "__loot:open_chest")
        "Bạn mở Rương và nhận vật phẩm cùng ${resolution.coreReward} Core."
        else "Bạn phát hiện một Rương khi khám phá. Có thể mở Rương trước khi đi tiếp."
      ExplorationOutcome.ENTITY -> "Một thực thể xuất hiện trên đường khám phá."
      ExplorationOutcome.NONE -> if (action.trim() == "__loot:open_chest")
        "Không có Rương để mở." else "Bạn tiếp tục khảo sát Level ${committed.state.levelRuntime.key}."
    }
    appendLog(result, if (action.trim() == "__loot:open_chest") "Mở Rương" else action, reply)
    logger.log(PipelineLogEvent("EXPLORER_COMMIT", turnId = turnId, details = mapOf("outcome" to resolution.outcome.name)))
    return response(true, result, null, "explorer_committed", reply)
  }

  fun openChest(stateJson: String): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    val resolution = ExplorationRuntime.openChest(state)
    repository.save(resolution.state)
    return JSONObject().apply {
      put("handled", resolution.outcome == ExplorationOutcome.CHEST)
      put("outcome", resolution.outcome.name)
      resolution.payloadKey?.let { put("itemId", it) }
      if (resolution.coreReward > 0) put("coreReward", resolution.coreReward)
      put("state", syncUiState(ui, resolution.state, false))
    }.toString()
  }


  fun processItemAction(
    stateJson: String,
    ownerId: String,
    itemId: String,
    operation: String,
    targetId: String,
    quantity: Int
  ): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    if (CombatRuntime.active(state) != null) {
      return response(false, syncUiState(ui, state, false), "combat_locked", "item_action_rejected")
    }
    val actorId = resolveCharacterId(state, ownerId) ?: state.party.leaderId
    val inventory = state.inventories[actorId] ?: InventoryState(actorId)
    val stack = inventory.items[itemId]
      ?: inventory.items.values.firstOrNull { it.itemId == itemId || it.name.equals(itemId, true) }
      ?: return response(false, syncUiState(ui, state, false), "item_not_owned", "item_action_rejected")
    val op = operation.trim().lowercase()
    val target = when (op) {
      "share" -> resolveCharacterId(state, targetId)
        ?: return response(false, syncUiState(ui, state, false), "target_unknown", "item_action_rejected")
      "use" -> actorId
      else -> null
    }
    val commandOperation = when (op) {
      "use", "share" -> ItemCommand.Operation.USE
      "discard", "drop" -> ItemCommand.Operation.DROP
      else -> return response(false, syncUiState(ui, state, false), "item_action_invalid", "item_action_rejected")
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
      return response(false, syncUiState(ui, state, false), reason, "item_action_rejected")
    }
    repository.save(execution.state)
    return response(true, syncUiState(ui, execution.state, false), null, "item_action_committed", eventReply(execution.events))
  }

  fun processCoreUpgrade(stateJson: String, characterId: String, stat: String): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    val targetId = resolveCharacterId(state, characterId)
      ?: return response(false, syncUiState(ui, state, false), "target_unknown", "core_upgrade_rejected")
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
      return response(false, syncUiState(ui, state, false), reason, "core_upgrade_rejected")
    }
    repository.save(execution.state)
    return response(true, syncUiState(ui, execution.state, false), null, "core_upgrade_committed", "Đã nâng chỉ số.")
  }

  fun combatState(stateJson: String): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    return JSONObject().apply {
      put("handled", CombatRuntime.active(state) != null)
      put("state", syncUiState(ui, state, false))
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
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    return try {
      val resolution = CombatRuntime.resolveFinalizedHand(state)
      repository.save(resolution.state)
      JSONObject().apply {
        put("handled", resolution.handled)
        put("state", syncUiState(ui, resolution.state, false))
        put("reply", resolution.reply)
        CombatRuntime.toJson(resolution.state)?.let { put("combat", it) }
      }.toString()
    } catch (error: Exception) {
      response(false, syncUiState(ui, state, false), error.message ?: "combat_resolve_failed", "combat_resolve_rejected")
    }
  }

  private fun mutateCombat(stateJson: String, mutation: (GameState) -> GameState): String {
    val ui = JSONObject(stateJson)
    val state = loadState(ui)
    return try {
      val next = mutation(state)
      repository.save(next)
      JSONObject().apply {
        put("handled", true)
        put("state", syncUiState(ui, next, false))
        CombatRuntime.toJson(next)?.let { put("combat", it) }
      }.toString()
    } catch (error: Exception) {
      response(false, syncUiState(ui, state, false), error.message ?: "combat_action_failed", "combat_action_rejected")
    }
  }

  fun clear() = repository.clear()
  override fun close() = Unit

  /**
   * Commits only the gameplay delta already accepted by the ui canon/dice validator.
   * Candidate prose/JSON never becomes item authority: V2 loot stays Core/System-owned.
   * Party/world deltas are validated and then projected back onto the UI state.
   */
  fun processValidatedCandidate(beforeJson: String, candidateJson: String, action: String): String {
    val before = JSONObject(beforeJson)
    val candidate = JSONObject(candidateJson)
    val core = loadState(before)
    val turnId = nextTurnId(before, core)
    val pending = TurnCoordinator.createPending(core, turnId, action)
    if (pending.error != null) return response(false, before, pending.error, "pending_rejected")
    val commands = mutableListOf<GameCommand>()
    // V2 authority: AI narration cannot create, remove, transfer or otherwise mutate Inventory.
    val inventoryLocked = true

    val desiredParty = mutableMapOf<String, JSONObject>()
    val partyJson = candidate.optJSONArray("party") ?: JSONArray()
    for (index in 0 until partyJson.length()) {
      val member = partyJson.optJSONObject(index) ?: continue
      val id = member.optString("id").ifBlank { member.optString("name").trim().lowercase() }
      if (id.isNotBlank()) desiredParty[id] = member
    }
    val currentFollowers = pending.state.party.memberIds.filter { it != PLAYER_ID }.toSet()
    (currentFollowers - desiredParty.keys).sorted().forEachIndexed { index, id ->
      commands += PartyCommand("$turnId:GEMINI:PARTY_REMOVE:$index", turnId, PLAYER_ID, id, CommandSource.GEMINI, PartyCommand.Operation.REMOVE)
    }
    (desiredParty.keys - currentFollowers).sorted().forEachIndexed { index, id ->
      val member = desiredParty.getValue(id)
      val known = pending.state.characters[id]
      commands += PartyCommand(
        "$turnId:GEMINI:PARTY_ADD:$index", turnId, PLAYER_ID, id, CommandSource.GEMINI, PartyCommand.Operation.ADD,
        consentConfirmed = member.optBoolean("joinConfirmed", false) && known?.metadata?.get("joinEligible") == "true",
        targetPresent = member.optBoolean("present", false) && known?.presence == CharacterPresence.ACTIVE
      )
    }
    commands += ValidatedStateCommand(
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
    val synchronized = syncUiState(candidate, committed.state, incrementTurn = false)
    logger.log(PipelineLogEvent("GEMINI_COMMIT", turnId = turnId, source = CommandSource.GEMINI, details = mapOf("commands" to commands.size.toString(), "inventoryLocked" to inventoryLocked.toString())))
    return response(true, synchronized, null, "gemini_delta_committed")
  }

  private fun loadState(ui: JSONObject): GameState {
    if (repository.exists()) return repository.load()
    val migrated = GameStateCodec.decode(ui)
    repository.save(migrated)
    return migrated
  }

  private fun contextFor(state: GameState): GameContext {
    val actors = buildMap {
      state.characters.values.forEach { character ->
        put(character.id.lowercase(), character.id)
        put(character.name.lowercase(), character.id)
      }
    }
    val items = state.inventories.values.flatMap { it.items.values }
      .associate { it.name.lowercase() to it.itemId }
    return GameContext(state, actors, items)
  }

  private fun isDirectPlayerPickupAction(action: String): Boolean {
    val text = action.trim()
    val directVerb = Regex("(?:^|\\s)(?:nhặt|lượm|cầm\\s+lên|lấy(?:\\s+lên)?|thu\\s+hồi|tịch\\s+thu|nhận(?:\\s+lấy)?|pick\\s+up|take|receive)(?:\\s|$)", RegexOption.IGNORE_CASE)
    val inventoryAssertion = Regex("(?:thêm|bỏ|đưa).{0,80}(?:vào|trong)\\s+(?:inventory|kho đồ|túi đồ)", RegexOption.IGNORE_CASE)
    return directVerb.containsMatchIn(text) || inventoryAssertion.containsMatchIn(text)
  }

  private fun nextTurnId(ui: JSONObject, state: GameState): String {
    val number = ui.optInt("turn", state.turn.currentTurnId.substringAfterLast('_').toIntOrNull() ?: 1)
    return "TURN_${number.coerceAtLeast(1)}"
  }

  private fun timeAdvanceCommand(turnId: String, action: String): TimeAdvanceCommand = TimeAdvanceCommand(
    commandId = "$turnId:SYSTEM:TIME",
    turnId = turnId,
    actorId = PLAYER_ID,
    source = CommandSource.SYSTEM,
    minutes = TimeCostPolicy.estimateMinutes(action),
    reason = "player_action"
  )

  private fun resolveCharacterId(state: GameState, raw: String?): String? {
    val value = raw.orEmpty().trim()
    if (value.isBlank()) return null
    state.characters[value]?.let { return it.id }
    val normalized = value.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_')
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

  private fun clientItem(stack: ItemStack): JSONObject = JSONObject().apply {
    put("id", stack.itemId)
    put("name", stack.name)
    put("quantity", stack.quantity)
    stack.condition?.let { put("state", it) }
    put("metadata", JSONObject(stack.metadata))
    GameplayCatalog.itemFor(stack)?.let { item ->
      put("kind", "consumable")
      put("category", item.category)
      put("stackable", true)
      put("effects", JSONObject().apply {
        if (item.effect.hunger > 0) put("hunger", item.effect.hunger)
        if (item.effect.thirst > 0) put("thirst", item.effect.thirst)
        if (item.effect.hp > 0) put("hp", item.effect.hp)
      })
    }
  }

  private fun syncUiState(ui: JSONObject, state: GameState, incrementTurn: Boolean): JSONObject {
    val output = JSONObject(ui.toString())
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
      put("lastResult", state.levelRuntime.route.lastResult.name)
    })
    output.put("currentLevelKey", state.levelRuntime.key)
    levelGraph.node(state.levelRuntime.key)?.parentLevel?.let { output.put("currentLevel", it) }
    val chestPresent = ExplorationRuntime.chestPresent(state)
    output.put("chestPresent", chestPresent)
    val flags = output.optJSONObject("flags") ?: JSONObject()
    state.world["flagsJson"]?.let { raw ->
      val projected = JSONObject(raw)
      projected.keys().forEach { key -> flags.put(key, projected.get(key)) }
    }
    flags.put("chestPresent", chestPresent)
    output.put("flags", flags)
    CombatRuntime.toJson(state)?.let { output.put("combat", it) }
    val playerInventory = state.inventories[PLAYER_ID]?.items?.values.orEmpty()
    output.put("inventory", JSONArray().apply { playerInventory.forEach { put(clientItem(it)) } })
    output.put("party", JSONArray().apply { state.party.memberIds.filter { it != PLAYER_ID }.forEach { id ->
      state.characters[id]?.let { character -> put(JSONObject().apply {
        put("id", character.id); put("name", character.name); character.avatarRef?.let { put("avatar", it) }
        put("presence", character.presence.name)
        put("joined", true)
        put("inventory", JSONArray().apply {
          state.inventories[id]?.items?.values.orEmpty().forEach { put(clientItem(it)) }
        })
      }) }
    } })
    state.world["location"]?.let { output.put("location", it) }
    state.world["title"]?.let { output.put("title", it) }
    state.world["levelJson"]?.let { output.put("level", JSONObject(it)) }
    state.metadata["playerJson"]?.let { output.put("player", JSONObject(it)) }
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
    "inventory_remove" -> "Vật phẩm đã được loại khỏi Inventory theo hành động của Cao Minh."
    "inventory_transfer" -> "Vật phẩm đã được chuyển giao."
    "item_equipped" -> "Vật phẩm đã được trang bị."
    "item_unequipped" -> "Vật phẩm đã được tháo khỏi trang bị."
    "item_consumed", "hp_restored", "physiology_food_restored", "physiology_water_restored" -> "Vật phẩm đã được sử dụng."
    "character_stat_upgraded" -> "Chỉ số nhân vật đã được nâng."
    else -> "Hành động đã được Game State Core xác nhận."
  }

  private fun validationReply(reason: String): String {
    val message = when (reason) {
      "player_pickup_unavailable", "restore_narrative_only", "item_not_consumable" -> "This action is not available."
      "scan_source_missing", "scan_template_missing" -> "There is no object available for scanning or multiplying."
      "insufficient_item_quantity", "item_not_owned" -> "This action is not available."
      "party_full" -> "Party đã đủ tối đa bốn thành viên."
      "join_not_confirmed" -> "Yêu cầu gia nhập chưa đủ điều kiện hoặc chưa được NPC xác nhận."
      else -> "This action is not available."
    }
    return "[Warning] $message"
  }

  companion object {
    @JvmStatic fun create(context: Context, debugLogging: Boolean = false): GameCoreFacade = GameCoreFacade(
      SharedPreferencesSaveRepository(context.applicationContext),
      AndroidGamePipelineLogger(debugLogging),
      LevelGraph.load(context.applicationContext)
    )
  }
}
