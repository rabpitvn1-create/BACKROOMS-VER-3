package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.CharacterProgressionRules
import com.rabpit.backroom.core.gameplay.GameplayCatalog
import com.rabpit.backroom.core.gameplay.GameplayRng
import com.rabpit.backroom.core.gameplay.LevelGraph
import com.rabpit.backroom.core.gameplay.LevelRouteRules
import kotlin.math.expm1
import kotlin.math.ln

enum class ExplorationOutcome { NONE, ROUTE_PROGRESS, ROUTE_RESET, EXIT_AVAILABLE, LEVEL_TRANSITION, CHEST, ENTITY }

data class ExplorationResolution(
  val state: GameState,
  val outcome: ExplorationOutcome,
  val payloadKey: String? = null,
  val coreReward: Int = 0
)

object ExplorationRuntime {
  private const val CHEST_PRESENT = "loot.chestPresent"
  private const val LAST_OUTCOME = "explore.lastOutcome"
  private const val LAST_PAYLOAD = "explore.lastPayload"

  private data class Candidate(val outcome: ExplorationOutcome, val key: String, val probability: Double)

  fun resolve(state: GameState, action: String, graph: LevelGraph): ExplorationResolution {
    if (CombatRuntime.active(state) != null) return result(state, ExplorationOutcome.NONE)
    val rng = GameplayRng(state.turn.currentTurnId, state.turn.completedTurnIds.size)

    transitionTarget(state, action, graph)?.let { target ->
      val transitioned = LevelRouteRules.transition(state.levelRuntime, graph, target)
        ?: return result(state, ExplorationOutcome.NONE)
      val moved = state.copy(levelRuntime = transitioned)
      val (rewarded, reward) = CharacterProgressionRules.rewardStageCompletion(moved, transitioned.stageIndex)
      return result(rewarded, ExplorationOutcome.LEVEL_TRANSITION, target, reward)
    }

    var next = state.copy(levelRuntime = LevelRouteRules.roll(
      state.levelRuntime,
      turnNumber(state),
      state.levelRuntime.key,
      rng.nextInt(GameplayRng.Scope.ROUTE, 100)
    ))

    val candidates = mutableListOf<Candidate>()
    if (next.metadata[CHEST_PRESENT] != "true") {
      candidates += Candidate(ExplorationOutcome.CHEST, "chest", GameplayCatalog.CHEST_SPAWN_RATE_PERCENT / 100.0)
    }
    GameplayCatalog.allEntities().asSequence()
      .filter { it.autoSpawn }
      .forEach { entity ->
        val valid = if (entity.treasure) GameplayCatalog.validTreasureAutoSpawnRate(entity.autoSpawnRatePercent)
        else GameplayCatalog.validAutoSpawnRate(entity.autoSpawnRatePercent)
        if (valid) candidates += Candidate(
          ExplorationOutcome.ENTITY,
          entity.key,
          (entity.autoSpawnRatePercent / 100.0).coerceIn(0.0, 0.99999999)
        )
      }

    val selected = select(candidates, rng)
    if (selected != null) {
      if (selected.outcome == ExplorationOutcome.CHEST) {
        next = next.copy(metadata = next.metadata + (CHEST_PRESENT to "true"))
      } else if (selected.outcome == ExplorationOutcome.ENTITY) {
        next = CombatRuntime.start(next, selected.key)
      }
      return result(next, selected.outcome, selected.key)
    }

    val routeOutcome = when (next.levelRuntime.route.lastResult) {
      com.rabpit.backroom.core.gameplay.RouteResult.RESET -> ExplorationOutcome.ROUTE_RESET
      com.rabpit.backroom.core.gameplay.RouteResult.EXIT_AVAILABLE -> ExplorationOutcome.EXIT_AVAILABLE
      com.rabpit.backroom.core.gameplay.RouteResult.SUCCESS -> ExplorationOutcome.ROUTE_PROGRESS
      else -> ExplorationOutcome.NONE
    }
    return result(next, routeOutcome)
  }

  fun openChest(state: GameState): ExplorationResolution {
    if (state.metadata[CHEST_PRESENT] != "true") return result(state, ExplorationOutcome.NONE)
    val rng = GameplayRng(state.turn.currentTurnId + ":CHEST", state.turn.completedTurnIds.size)
    val item = GameplayCatalog.chestPool[rng.nextInt(GameplayRng.Scope.LOOT, GameplayCatalog.chestPool.size)]
    val pickup = StateReducer.execute(state, ItemCommand(
      commandId = nextCommandId(state, "CHEST_ITEM"),
      turnId = state.turn.currentTurnId,
      actorId = state.party.leaderId,
      source = CommandSource.SYSTEM,
      operation = ItemCommand.Operation.PICKUP,
      itemId = item.id,
      itemName = item.name,
      metadata = mapOf("lootSource" to "chest")
    ))
    if (!pickup.applied) return result(state, ExplorationOutcome.CHEST, item.id)

    val (rewarded, coreReward) = CharacterProgressionRules.grantCore(
      pickup.state,
      CharacterProgressionRules.bundleSize(state.levelRuntime.stageIndex)
    )
    val cleared = rewarded.copy(metadata = rewarded.metadata - CHEST_PRESENT)
    return result(cleared, ExplorationOutcome.CHEST, item.id, coreReward)
  }

  fun entityVictoryRewards(state: GameState, entityKey: String): GameState {
    val rng = GameplayRng(state.turn.currentTurnId + ":ENTITY:$entityKey", state.turn.completedTurnIds.size)
    var next = state
    if (GameplayCatalog.ENTITY_DROP_RATE_PERCENT == 100) {
      val item = GameplayCatalog.entityDropPool[rng.nextInt(GameplayRng.Scope.LOOT, GameplayCatalog.entityDropPool.size)]
      val pickup = StateReducer.execute(next, ItemCommand(
        commandId = nextCommandId(next, "ENTITY_LOOT"),
        turnId = next.turn.currentTurnId,
        actorId = next.party.leaderId,
        source = CommandSource.SYSTEM,
        operation = ItemCommand.Operation.PICKUP,
        itemId = item.id,
        itemName = item.name,
        metadata = mapOf("lootSource" to "entity:$entityKey")
      ))
      if (pickup.applied) next = pickup.state
    }

    val stage = next.levelRuntime.stageIndex
    val reward = if (entityKey == "tam_ma_cao_minh") {
      val firstToken = "$entityKey@${stage.coerceAtLeast(0)}"
      val first = firstToken !in next.coreResource.treasureStageKills
      val base = if (first) 100 else 10
      val scaled = ((base.toLong() * (100 + 10 * stage.coerceAtLeast(0)) + 50L) / 100L)
        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
      CharacterProgressionRules.rewardTreasureEntityVictory(next, entityKey, stage, scaled, scaled)
    } else {
      CharacterProgressionRules.grantCore(
        next,
        CharacterProgressionRules.scaledCoreReward(if (entityKey == "copx") 5 else CharacterProgressionRules.ENTITY_VICTORY_BASE_CORE, stage)
      )
    }
    return reward.first
  }

  fun chestPresent(state: GameState): Boolean = state.metadata[CHEST_PRESENT] == "true"

  private fun transitionTarget(state: GameState, action: String, graph: LevelGraph): String? {
    if (!state.levelRuntime.route.exitAvailable) return null
    Regex("(?i)(?:^|\\b)level\\s*([0-6](?:\\.[0-9]+)?)").find(action)
      ?.groupValues?.getOrNull(1)?.let { if (graph.allows(state.levelRuntime.key, it)) return it }
    val transitionIntent = listOf("đi qua", "bước qua", "tiến vào", "đi vào", "enter", "go through", "cross")
      .any { action.contains(it, ignoreCase = true) }
    return if (transitionIntent) graph.next(state.levelRuntime.key) else null
  }

  private fun select(candidates: List<Candidate>, rng: GameplayRng): Candidate? {
    val weighted = candidates.mapNotNull { candidate ->
      val p = candidate.probability
      if (p <= 0.0) null else candidate to -ln(1.0 - p)
    }
    val totalHazard = weighted.sumOf { it.second }
    if (totalHazard <= 0.0) return null
    val eventOdds = expm1(totalHazard)
    val candidateWeights = weighted.map { (candidate, hazard) ->
      candidate to (eventOdds * hazard / totalHazard)
    }
    val totalWeight = 1.0 + candidateWeights.sumOf { it.second }
    var cursor = rng.nextUnit(GameplayRng.Scope.SITUATION_SELECTION) * totalWeight
    if (cursor < 1.0) return null
    cursor -= 1.0
    candidateWeights.forEach { (candidate, weight) ->
      if (cursor < weight) return candidate
      cursor -= weight
    }
    return candidateWeights.lastOrNull()?.first
  }

  private fun result(state: GameState, outcome: ExplorationOutcome, key: String? = null, coreReward: Int = 0): ExplorationResolution {
    val metadata = state.metadata + (LAST_OUTCOME to outcome.name) +
      if (key == null) emptyMap() else mapOf(LAST_PAYLOAD to key)
    return ExplorationResolution(state.copy(metadata = metadata), outcome, key, coreReward)
  }

  private fun turnNumber(state: GameState) =
    state.turn.currentTurnId.substringAfterLast('_').toIntOrNull()?.coerceAtLeast(1) ?: 1

  private fun nextCommandId(state: GameState, kind: String): String {
    var sequence = state.turn.executedCommandIds.size
    var id: String
    do id = "${state.turn.currentTurnId}:SYSTEM:$kind:${sequence++}"
    while (id in state.turn.executedCommandIds)
    return id
  }
}
