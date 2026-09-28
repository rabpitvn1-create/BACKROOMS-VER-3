package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.CharacterProgressionState
import com.rabpit.backroom.core.gameplay.CharacterStatsState
import com.rabpit.backroom.core.gameplay.CoreResourceState
import com.rabpit.backroom.core.gameplay.LevelRouteState
import com.rabpit.backroom.core.gameplay.LevelRuntimeState
import com.rabpit.backroom.core.gameplay.RouteResult

import org.json.JSONArray
import org.json.JSONObject

object GameStateCodec {
  fun encode(state: GameState): String = JSONObject().apply {
    put("saveVersion", state.saveVersion)
    put("characters", JSONObject().apply { state.characters.forEach { (id, value) -> put(id, character(value)) } })
    put("party", JSONObject().apply {
      put("leaderId", state.party.leaderId)
      put("memberIds", JSONArray(state.party.memberIds))
      put("maxMembers", state.party.maxMembers)
    })
    put("inventories", JSONObject().apply { state.inventories.forEach { (id, value) -> put(id, inventory(value)) } })
    put("equipment", JSONObject().apply { state.equipment.forEach { (id, value) -> put(id, equipment(value)) } })
    put("statuses", JSONObject().apply { state.statuses.forEach { (id, value) -> put(id, status(value)) } })
    put("turn", turn(state.turn))
    put("time", gameTime(state.time))
    put("world", stringMap(state.world))
    put("metadata", stringMap(state.metadata))
    put("coreResource", coreResource(state.coreResource))
    put("levelRuntime", levelRuntime(state.levelRuntime))
  }.toString()

  fun decode(raw: String): GameState = decode(JSONObject(raw))

  fun decode(root: JSONObject): GameState =
    if (root.optInt("saveVersion", 0) == CURRENT_SAVE_VERSION) decodeCurrent(root)
    else GameState.initial()

  private fun decodeCurrent(root: JSONObject): GameState {
    val characters = root.optJSONObject("characters").objectMap(::decodeCharacter)
    val inventories = root.optJSONObject("inventories").objectMap(::decodeInventory)
    val equipment = root.optJSONObject("equipment").objectMap(::decodeEquipment)
    val statuses = root.optJSONObject("statuses").objectMap(::decodeStatus)
    val partyJson = root.optJSONObject("party") ?: JSONObject()
    val party = PartyState(
      leaderId = partyJson.optString("leaderId", KAI_ID),
      memberIds = partyJson.optJSONArray("memberIds").strings().ifEmpty { listOf(KAI_ID) },
      maxMembers = partyJson.optInt("maxMembers", 4).coerceAtLeast(1)
    )
    return GameState(
      characters = characters.ifEmpty { GameState.initial().characters },
      party = party,
      inventories = inventories.ifEmpty { GameState.initial().inventories },
      equipment = equipment.ifEmpty { GameState.initial().equipment },
      statuses = statuses,
      turn = decodeTurn(root.optJSONObject("turn") ?: JSONObject()),
      time = decodeGameTime(root.optJSONObject("time")),
      world = root.optJSONObject("world").stringsMap(),
      saveVersion = CURRENT_SAVE_VERSION,
      metadata = root.optJSONObject("metadata").stringsMap(),
      coreResource = decodeCoreResource(root.optJSONObject("coreResource")),
      levelRuntime = decodeLevelRuntime(root.optJSONObject("levelRuntime"))
    )
  }

  private fun character(value: CharacterState) = JSONObject().apply {
    put("id", value.id); put("name", value.name); putNullable("avatarRef", value.avatarRef)
    putNullable("healthState", value.healthState); put("injuries", JSONArray(value.injuries))
    put("presence", value.presence.name); put("inventoryId", value.inventoryId); put("equipmentId", value.equipmentId)
    put("statusIds", JSONArray(value.statusIds.toList())); put("physiology", physiology(value.physiology)); put("metadata", stringMap(value.metadata))
    put("progression", progression(value.progression))
  }

  private fun decodeCharacter(json: JSONObject) = CharacterState(
    id = json.optString("id"), name = json.optString("name"),
    avatarRef = json.nullableString("avatarRef"), healthState = json.nullableString("healthState"),
    injuries = json.optJSONArray("injuries").strings(),
    presence = enumOr(CharacterPresence.ACTIVE, json.optString("presence")),
    inventoryId = json.optString("inventoryId", json.optString("id")),
    equipmentId = json.optString("equipmentId", json.optString("id")),
    statusIds = json.optJSONArray("statusIds").strings().toSet(),
    physiology = decodePhysiology(json.optJSONObject("physiology")),
    metadata = json.optJSONObject("metadata").stringsMap(),
    progression = decodeProgression(json.optJSONObject("progression"))
  )

  private fun physiology(value: PhysiologyState) = JSONObject().apply {
    putNullable("minutesSinceFood", value.minutesSinceFood)
    putNullable("minutesSinceWater", value.minutesSinceWater)
    putNullable("minutesAwake", value.minutesAwake)
    putNullable("painState", value.painState)
    putNullable("infectionState", value.infectionState)
    putNullable("thermalState", value.thermalState)
    put("metadata", stringMap(value.metadata))
  }

  private fun decodePhysiology(json: JSONObject?): PhysiologyState {
    if (json == null) return PhysiologyState()
    return PhysiologyState(
      minutesSinceFood = json.nullableLong("minutesSinceFood")?.coerceAtLeast(0L),
      minutesSinceWater = json.nullableLong("minutesSinceWater")?.coerceAtLeast(0L),
      minutesAwake = json.nullableLong("minutesAwake")?.coerceAtLeast(0L),
      painState = json.nullableString("painState"),
      infectionState = json.nullableString("infectionState"),
      thermalState = json.nullableString("thermalState"),
      metadata = json.optJSONObject("metadata").stringsMap()
    )
  }

  private fun progression(value: CharacterProgressionState) = JSONObject().apply {
    put("baseMaxHp", value.baseMaxHp)
    put("currentHp", value.currentHp)
    put("stats", JSONObject().apply {
      put("STR", value.stats.str); put("DEF", value.stats.def); put("SKL", value.stats.skl); put("VIT", value.stats.vit)
    })
    putNullable("downedAtTurn", value.downedAtTurn)
    putNullable("reviveAtTurn", value.reviveAtTurn)
  }

  private fun decodeProgression(json: JSONObject?): CharacterProgressionState {
    if (json == null) return CharacterProgressionState()
    val stats = json.optJSONObject("stats")
    return CharacterProgressionState(
      baseMaxHp = json.optInt("baseMaxHp", 50).coerceAtLeast(1),
      currentHp = json.optInt("currentHp", json.optInt("baseMaxHp", 50)).coerceAtLeast(0),
      stats = CharacterStatsState(
        str = stats?.optInt("STR", 5) ?: 5,
        def = stats?.optInt("DEF", 5) ?: 5,
        skl = stats?.optInt("SKL", 5) ?: 5,
        vit = stats?.optInt("VIT", 5) ?: 5
      ).normalized(),
      downedAtTurn = json.nullableInt("downedAtTurn"),
      reviveAtTurn = json.nullableInt("reviveAtTurn")
    )
  }

  private fun coreResource(value: CoreResourceState) = JSONObject().apply {
    put("quantity", value.quantity)
    put("highestRewardedStageIndex", value.highestRewardedStageIndex)
    put("treasureStageKills", JSONArray(value.treasureStageKills.sorted()))
  }

  private fun decodeCoreResource(json: JSONObject?): CoreResourceState {
    if (json == null) return CoreResourceState()
    return CoreResourceState(
      quantity = json.optInt("quantity", 0).coerceAtLeast(0),
      highestRewardedStageIndex = json.optInt("highestRewardedStageIndex", -1).coerceAtLeast(-1),
      treasureStageKills = json.optJSONArray("treasureStageKills").strings().toSet()
    )
  }

  private fun levelRuntime(value: LevelRuntimeState) = JSONObject().apply {
    put("key", value.key)
    put("stageIndex", value.stageIndex)
    put("route", JSONObject().apply {
      put("streak", value.route.streak)
      put("exitAvailable", value.route.exitAvailable)
      putNullable("lastRollTurn", value.route.lastRollTurn)
      put("lastResult", value.route.lastResult.name)
      putNullable("originLocation", value.route.originLocation)
      putNullable("returnLocation", value.route.returnLocation)
    })
  }

  private fun decodeLevelRuntime(json: JSONObject?): LevelRuntimeState {
    if (json == null) return LevelRuntimeState()
    val route = json.optJSONObject("route")
    return LevelRuntimeState(
      key = json.optString("key", "0").ifBlank { "0" },
      stageIndex = json.optInt("stageIndex", 0).coerceAtLeast(0),
      route = LevelRouteState(
        streak = route?.optInt("streak", 0)?.coerceIn(0, 6) ?: 0,
        exitAvailable = route?.optBoolean("exitAvailable", false) ?: false,
        lastRollTurn = route?.nullableInt("lastRollTurn"),
        lastResult = enumOr(RouteResult.NONE, route?.optString("lastResult").orEmpty()),
        originLocation = route?.nullableString("originLocation"),
        returnLocation = route?.nullableString("returnLocation")
      )
    )
  }

  private fun item(value: ItemStack) = JSONObject().apply {
    val normalized = ItemContentRules.normalize(value)
    put("itemId", normalized.itemId); put("name", normalized.name); put("quantity", normalized.quantity)
    putNullable("condition", normalized.condition); put("metadata", stringMap(normalized.metadata))
    put("archetypeId", normalized.archetypeId); put("contentState", normalized.contentState.name)
  }

  private fun decodeItem(json: JSONObject): ItemStack = ItemContentRules.normalize(ItemStack(
    itemId = json.optString("itemId"),
    name = json.optString("name"),
    quantity = json.optInt("quantity", 1).coerceAtLeast(1),
    condition = json.nullableString("condition"),
    metadata = json.optJSONObject("metadata").stringsMap(),
    archetypeId = json.optString("archetypeId", json.optString("itemId")),
    contentState = enumOr(ContentState.NONE, json.optString("contentState"))
  ))

  private fun itemMap(json: JSONObject?): Map<String, ItemStack> {
    if (json == null) return emptyMap()
    val result = linkedMapOf<String, ItemStack>()
    json.keys().forEach { key ->
      val decoded = json.optJSONObject(key)?.let(::decodeItem) ?: return@forEach
      val old = result[decoded.itemId]
      result[decoded.itemId] = if (old != null && ItemContentRules.sameStackState(old, decoded)) old.copy(quantity = old.quantity + decoded.quantity) else decoded
    }
    return result
  }

  private fun inventory(value: InventoryState) = JSONObject().apply {
    put("ownerId", value.ownerId); put("items", JSONObject().apply { value.items.values.forEach { stack -> put(ItemContentRules.normalize(stack).itemId, item(stack)) } })
  }

  private fun decodeInventory(json: JSONObject) = InventoryState(json.optString("ownerId"), itemMap(json.optJSONObject("items")))

  private fun equipment(value: EquipmentState) = JSONObject().apply { put("ownerId", value.ownerId); put("slots", stringMap(value.slots)) }
  private fun decodeEquipment(json: JSONObject) = EquipmentState(json.optString("ownerId"), json.optJSONObject("slots").stringsMap())

  private fun status(value: StatusEffect) = JSONObject().apply {
    put("id", value.id); put("type", value.type); put("source", value.source); putNullable("startTurnId", value.startTurnId)
    putNullable("durationTurns", value.durationTurns); put("persistent", value.persistent); put("metadata", stringMap(value.metadata))
  }

  private fun decodeStatus(json: JSONObject) = StatusEffect(
    json.optString("id"), json.optString("type"), json.optString("source"), json.nullableString("startTurnId"),
    if (json.has("durationTurns") && !json.isNull("durationTurns")) json.optInt("durationTurns") else null,
    json.optBoolean("persistent"), json.optJSONObject("metadata").stringsMap()
  )

  private fun turn(value: TurnState) = JSONObject().apply {
    put("currentTurnId", value.currentTurnId); putNullable("pending", value.pending?.let(::pending))
    put("completedTurnIds", JSONArray(value.completedTurnIds.toList())); put("executedCommandIds", JSONArray(value.executedCommandIds.toList()))
  }

  private fun pending(value: PendingTurn) = JSONObject().apply {
    put("turnId", value.turnId); put("input", value.input); put("status", value.status.name)
    put("commandIds", JSONArray(value.commandIds)); putNullable("error", value.error)
  }

  private fun decodeTurn(json: JSONObject): TurnState {
    val pendingJson = json.optJSONObject("pending")
    val pending = pendingJson?.let { PendingTurn(it.optString("turnId"), it.optString("input"), enumOr(PendingTurnStatus.CREATED, it.optString("status")), it.optJSONArray("commandIds").strings(), it.nullableString("error")) }
    return TurnState(json.optString("currentTurnId", "TURN_1"), pending, json.optJSONArray("completedTurnIds").strings().toSet(), json.optJSONArray("executedCommandIds").strings().toSet())
  }

  private fun gameTime(value: GameTimeState) = JSONObject().apply {
    put("elapsedSubjectiveMinutes", value.elapsedSubjectiveMinutes)
    put("lastAdvanceMinutes", value.lastAdvanceMinutes)
    putNullable("lastAdvanceReason", value.lastAdvanceReason)
  }

  private fun decodeGameTime(json: JSONObject?): GameTimeState {
    if (json == null) return GameTimeState()
    return GameTimeState(
      elapsedSubjectiveMinutes = json.optLong("elapsedSubjectiveMinutes", 0L).coerceAtLeast(0L),
      lastAdvanceMinutes = json.optInt("lastAdvanceMinutes", 0).coerceAtLeast(0),
      lastAdvanceReason = json.nullableString("lastAdvanceReason")
    )
  }

  private fun stringMap(values: Map<String, String>) = JSONObject().apply { values.forEach { (key, value) -> put(key, value) } }
}

private fun JSONObject.putNullable(key: String, value: Any?) { put(key, value ?: JSONObject.NULL) }
private fun JSONObject.nullableString(key: String): String? = if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
private fun JSONObject.nullableLong(key: String): Long? = if (!has(key) || isNull(key)) null else optLong(key)
private fun JSONObject.nullableInt(key: String): Int? = if (!has(key) || isNull(key)) null else optInt(key)
private fun JSONObject?.stringsMap(): Map<String, String> {
  if (this == null) return emptyMap()
  val result = mutableMapOf<String, String>(); keys().forEach { result[it] = optString(it) }; return result
}
private fun <T> JSONObject?.objectMap(decode: (JSONObject) -> T): Map<String, T> {
  if (this == null) return emptyMap()
  val result = mutableMapOf<String, T>(); keys().forEach { key -> optJSONObject(key)?.let { result[key] = decode(it) } }; return result
}
private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }
private fun JSONArray?.objects(): List<JSONObject> = if (this == null) emptyList() else (0 until length()).mapNotNull(::optJSONObject)
private inline fun <reified T : Enum<T>> enumOr(fallback: T, value: String): T = enumValues<T>().firstOrNull { it.name == value } ?: fallback
