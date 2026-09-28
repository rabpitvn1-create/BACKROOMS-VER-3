package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.CharacterState
import com.rabpit.backroom.core.GameState
import com.rabpit.backroom.core.PhysiologyBand
import com.rabpit.backroom.core.PhysiologyStatusPolicy
import com.rabpit.backroom.core.StatusEffect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

enum class CharacterStat { STR, DEF, SKL, VIT }

data class CharacterStatsState(
  val str: Int = CharacterProgressionRules.BASE_STAT,
  val def: Int = CharacterProgressionRules.BASE_STAT,
  val skl: Int = CharacterProgressionRules.BASE_STAT,
  val vit: Int = CharacterProgressionRules.BASE_STAT
) {
  fun value(stat: CharacterStat): Int = when (stat) {
    CharacterStat.STR -> str
    CharacterStat.DEF -> def
    CharacterStat.SKL -> skl
    CharacterStat.VIT -> vit
  }

  fun with(stat: CharacterStat, value: Int): CharacterStatsState = when (stat) {
    CharacterStat.STR -> copy(str = value)
    CharacterStat.DEF -> copy(def = value)
    CharacterStat.SKL -> copy(skl = value)
    CharacterStat.VIT -> copy(vit = value)
  }

  fun normalized() = copy(
    str = str.coerceIn(CharacterProgressionRules.BASE_STAT, CharacterProgressionRules.MAX_STAT),
    def = def.coerceIn(CharacterProgressionRules.BASE_STAT, CharacterProgressionRules.MAX_STAT),
    skl = skl.coerceIn(CharacterProgressionRules.BASE_STAT, CharacterProgressionRules.MAX_STAT),
    vit = vit.coerceIn(CharacterProgressionRules.BASE_STAT, CharacterProgressionRules.MAX_STAT)
  )
}

data class CharacterProgressionState(
  val baseMaxHp: Int = CharacterProgressionRules.DEFAULT_BASE_MAX_HP,
  val currentHp: Int = CharacterProgressionRules.DEFAULT_BASE_MAX_HP,
  val stats: CharacterStatsState = CharacterStatsState(),
  val downedAtTurn: Int? = null,
  val reviveAtTurn: Int? = null
)

data class CoreResourceState(
  val quantity: Int = 0,
  val highestRewardedStageIndex: Int = -1,
  val treasureStageKills: Set<String> = emptySet()
)

data class StatLine(
  val base: Int,
  val passiveBonus: Int,
  val equipmentBonus: Int,
  val temporaryModifier: Int,
  val effective: Int,
  val nextCoreCost: Int
)

data class CombatStatProjection(
  val damage: Int,
  val defendPercent: Double,
  val criticalChancePercent: Int,
  val evasionPercent: Int,
  val resCriticalPercent: Int,
  val resEvasionPercent: Int
)

data class CharacterStatProjection(
  val currentHp: Int,
  val maxHp: Int,
  val baseMaxHp: Int,
  val stats: Map<CharacterStat, StatLine>,
  val combat: CombatStatProjection
)

data class StatUpgradeResult(
  val state: GameState,
  val characterId: String,
  val stat: CharacterStat,
  val value: Int,
  val cost: Int,
  val coreRemaining: Int
)

object CharacterProgressionRules {
  const val BASE_STAT = 5
  const val MAX_STAT = 999
  const val DEFAULT_BASE_MAX_HP = 50
  const val COMPANION_REVIVE_TURNS = 10
  const val ENTITY_VICTORY_BASE_CORE = 2

  fun upgradeCost(currentStat: Int) = 1 + (max(BASE_STAT, currentStat) - BASE_STAT) / 2
  fun bundleSize(stageIndex: Int) = 1 + max(0, stageIndex) / 10
  fun statPercent(stat: Int) = 100 + 10 * (stat.coerceIn(1, MAX_STAT) - BASE_STAT)

  fun scaledCoreReward(baseReward: Int, stageIndex: Int): Int {
    val base = max(0, baseReward)
    if (base == 0) return 0
    val value = base * Math.pow(1.5, max(0, stageIndex).toDouble())
    return if (!value.isFinite() || value >= Int.MAX_VALUE) Int.MAX_VALUE else max(base, round(value).toInt())
  }

  fun scaledByStat(baseValue: Int, stat: Int): Int =
    ((max(0, baseValue).toLong() * statPercent(stat) + 50L) / 100L)
      .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

  fun maxHpFor(baseMaxHp: Int, vit: Int) = max(1, scaledByStat(max(1, baseMaxHp), vit))

  fun upgrade(state: GameState, characterId: String, rawStat: String): StatUpgradeResult {
    val stat = runCatching { CharacterStat.valueOf(rawStat.trim().uppercase()) }.getOrNull()
      ?: throw IllegalArgumentException("Only STR, DEF, SKL or VIT can be upgraded.")
    val character = state.characters[characterId] ?: throw IllegalArgumentException("Unknown character: $characterId")
    val progression = character.progression.copy(stats = character.progression.stats.normalized())
    val current = progression.stats.value(stat)
    if (current >= MAX_STAT) throw IllegalStateException("Stat is already at the limit.")
    val cost = upgradeCost(current)
    if (state.coreResource.quantity < cost) throw IllegalStateException("Not enough Core. Need $cost Core.")

    val before = CharacterStatRules.project(state, characterId) ?: error("Unable to project character stats.")
    val upgraded = character.copy(progression = progression.copy(stats = progression.stats.with(stat, current + 1)))
    var next = state.copy(
      characters = state.characters + (characterId to upgraded),
      coreResource = state.coreResource.copy(quantity = state.coreResource.quantity - cost)
    )
    val after = CharacterStatRules.project(next, characterId) ?: error("Unable to project upgraded stats.")
    if (stat == CharacterStat.VIT && before.currentHp > 0) {
      val hp = min(after.maxHp, before.currentHp + max(0, after.maxHp - before.maxHp))
      next = setCurrentHp(next, characterId, hp)
    }
    return StatUpgradeResult(next, characterId, stat, current + 1, cost, next.coreResource.quantity)
  }

  fun grantCore(state: GameState, amount: Int): Pair<GameState, Int> {
    if (amount <= 0) return state to 0
    val current = state.coreResource.quantity.coerceAtLeast(0)
    val granted = min(amount.toLong(), Int.MAX_VALUE.toLong() - current).toInt()
    return state.copy(coreResource = state.coreResource.copy(quantity = current + granted)) to granted
  }

  fun rewardStageCompletion(state: GameState, stageIndex: Int): Pair<GameState, Int> {
    val stage = max(0, stageIndex)
    if (stage <= state.coreResource.highestRewardedStageIndex) return state to 0
    val (next, granted) = grantCore(state, bundleSize(stage))
    return next.copy(coreResource = next.coreResource.copy(highestRewardedStageIndex = stage)) to granted
  }

  fun rewardTreasureEntityVictory(
    state: GameState,
    entityKey: String,
    stageIndex: Int,
    firstKillReward: Int,
    repeatKillReward: Int
  ): Pair<GameState, Int> {
    val token = "${entityKey.trim().lowercase()}@${max(0, stageIndex)}"
    val first = token !in state.coreResource.treasureStageKills
    val marked = if (first) state.coreResource.treasureStageKills + token else state.coreResource.treasureStageKills
    return grantCore(
      state.copy(coreResource = state.coreResource.copy(treasureStageKills = marked)),
      max(0, if (first) firstKillReward else repeatKillReward)
    )
  }

  fun setCurrentHp(state: GameState, characterId: String, hp: Int): GameState {
    val character = state.characters[characterId] ?: return state
    val maxHp = CharacterStatRules.project(state, characterId)?.maxHp
      ?: maxHpFor(character.progression.baseMaxHp, character.progression.stats.vit)
    return state.copy(characters = state.characters + (
      characterId to character.copy(progression = character.progression.copy(currentHp = hp.coerceIn(0, maxHp)))
    ))
  }

  fun heal(state: GameState, characterId: String, amount: Int): Pair<GameState, Int> {
    if (amount <= 0) return state to 0
    val projection = CharacterStatRules.project(state, characterId) ?: return state to 0
    if (characterId != state.party.leaderId && projection.currentHp <= 0) return state to 0
    val nextHp = min(projection.maxHp, projection.currentHp + amount)
    return setCurrentHp(state, characterId, nextHp) to (nextHp - projection.currentHp)
  }

  fun markCompanionDown(state: GameState, characterId: String): GameState {
    val character = state.characters[characterId] ?: return state
    val turn = turnNumber(state)
    val progression = character.progression.copy(
      currentHp = 0,
      downedAtTurn = character.progression.downedAtTurn ?: turn,
      reviveAtTurn = character.progression.reviveAtTurn ?: turn + COMPANION_REVIVE_TURNS
    )
    return state.copy(characters = state.characters + (characterId to character.copy(progression = progression)))
  }

  fun recoverDownedCompanions(state: GameState): GameState {
    val turn = turnNumber(state)
    val characters = state.characters.toMutableMap()
    var changed = false
    state.party.memberIds.filter { it != state.party.leaderId }.forEach { id ->
      val character = characters[id] ?: return@forEach
      val reviveAt = character.progression.reviveAtTurn ?: return@forEach
      if (character.progression.currentHp <= 0 && turn >= reviveAt) {
        characters[id] = character.copy(
          progression = character.progression.copy(currentHp = 1, downedAtTurn = null, reviveAtTurn = null)
        )
        changed = true
      }
    }
    return if (changed) state.copy(characters = characters) else state
  }

  private fun turnNumber(state: GameState) =
    state.turn.currentTurnId.substringAfterLast('_').toIntOrNull()?.coerceAtLeast(1) ?: 1
}

object CharacterStatRules {
  fun project(state: GameState, characterId: String): CharacterStatProjection? {
    val character = state.characters[characterId] ?: return null
    val base = character.progression.stats.normalized()
    val passive = 0
    val equipment = EquipmentRules.bonuses(state, characterId)
    val effective = CharacterStat.entries.associateWith {
      effectiveStat(character, state.statuses.values, it, passive, equipment.forStat(it))
    }
    val maxHp = CharacterProgressionRules.maxHpFor(
      character.progression.baseMaxHp.coerceAtLeast(1),
      effective.getValue(CharacterStat.VIT)
    ) + equipment.hp.coerceAtLeast(0)
    val currentHp = character.progression.currentHp.coerceIn(0, maxHp)
    val lines = CharacterStat.entries.associateWith { stat ->
      val raw = base.value(stat)
      val value = effective.getValue(stat)
      val equipmentBonus = equipment.forStat(stat)
      StatLine(raw, passive, equipmentBonus, value - raw - passive - equipmentBonus, value, CharacterProgressionRules.upgradeCost(raw))
    }
    val str = effective.getValue(CharacterStat.STR)
    val def = effective.getValue(CharacterStat.DEF)
    val skl = effective.getValue(CharacterStat.SKL)
    val vit = effective.getValue(CharacterStat.VIT)
    val baseAttack = character.metadata["baseAttack"]?.toIntOrNull()?.coerceAtLeast(1)
      ?: EquipmentRules.weaponDamage(state, characterId, 30)
    return CharacterStatProjection(
      currentHp, maxHp, character.progression.baseMaxHp.coerceAtLeast(1), lines,
      CombatStatProjection(
        damage = CharacterProgressionRules.scaledByStat(baseAttack, str),
        defendPercent = defendPercent(def),
        criticalChancePercent = (5 + (skl - 5) * 2).coerceIn(0, 50),
        evasionPercent = (max(0, vit - 5) * 2).coerceAtMost(35),
        resCriticalPercent = (max(0, def - 5) * 2).coerceAtMost(50),
        resEvasionPercent = (max(0, skl - 5) * 2).coerceAtMost(50)
      )
    )
  }

  fun effectiveStat(
    character: CharacterState,
    statuses: Collection<StatusEffect>,
    stat: CharacterStat,
    passiveBonus: Int,
    equipmentBonus: Int = 0
  ): Int {
    val base = character.progression.stats.normalized().value(stat)
    val derived = PhysiologyStatusPolicy.derive(character.physiology)
    val band = when (stat) {
      CharacterStat.STR -> derived.hunger
      CharacterStat.VIT -> derived.thirst
      CharacterStat.DEF, CharacterStat.SKL -> derived.sleepDeprivation
    }
    val survival = when (band) {
      PhysiologyBand.MODERATE -> -1
      PhysiologyBand.SEVERE -> -2
      PhysiologyBand.CRITICAL -> -3
      else -> 0
    }
    val temporary = statuses.asSequence()
      .filter { it.id in character.statusIds && (it.durationTurns == null || it.durationTurns > 0) }
      .sumOf {
        (it.metadata[stat.name]?.toIntOrNull()
          ?: it.metadata["modifier.${stat.name}"]?.toIntOrNull()
          ?: 0).coerceIn(-5, 5)
      }
    return (base + passiveBonus + equipmentBonus + survival + temporary).coerceIn(1, CharacterProgressionRules.MAX_STAT)
  }

  fun defendPercent(def: Int): Double {
    val multiplier = CharacterProgressionRules.statPercent(def)
    return round((100.0 - 10_000.0 / multiplier) * 10.0) / 10.0
  }
}
