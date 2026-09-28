package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.CharacterProgressionRules
import com.rabpit.backroom.core.gameplay.CharacterStatRules
import com.rabpit.backroom.core.gameplay.GameplayCatalog
import com.rabpit.backroom.core.gameplay.PokerDiceRuntime
import com.rabpit.backroom.core.gameplay.PokerDiceRules
import com.rabpit.backroom.core.gameplay.PokerDiceState

import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Authoritative, save-persistent combat state stored in GameState.metadata. */
object CombatRuntime {
  private const val PREFIX = "combat."
  private const val PLAYER_HP = "combat.playerHp"
  private const val PLAYER_MAX_HP = "combat.playerMaxHp"

  enum class Phase { ACTIVE, RESOLVED }
  enum class RangeBand { CLOSE, NEAR, FAR }
  enum class Cover { EXPOSED, PARTIAL, HARD }
  enum class EntityCondition { HEALTHY, HURT, WOUNDED, CRITICAL, DESTROYED }
  enum class Intent { READ, ATTACK, EVADE, MOVE, GUARD, ESCAPE, OTHER }

  data class Profile(
    val key: String,
    val displayName: String,
    val maxHp: Int,
    val attack: Int,
    val armor: Int,
    val aggression: Int
  )

  data class Snapshot(
    val encounterId: String,
    val entityKey: String,
    val entityName: String,
    val phase: Phase,
    val playerHp: Int,
    val playerMaxHp: Int,
    val entityHp: Int,
    val entityMaxHp: Int,
    val entityCondition: EntityCondition,
    val range: RangeBand,
    val cover: Cover,
    val momentum: Int,
    val opening: Int,
    val escapeProgress: Int,
    val noise: Int,
    val telegraph: String,
    val telegraphRevealed: Boolean,
    val eventCounter: Int,
    val seed: Long
  )

  data class Resolution(
    val state: GameState,
    val handled: Boolean,
    val reply: String = "",
    val entityDestroyed: Boolean = false,
    val escaped: Boolean = false
  )

  private data class Tuning(val armor: Int, val aggression: Int)

  private val tuning = mapOf(
    "hound" to Tuning(2,8), "clump" to Tuning(5,7), "duller" to Tuning(3,6),
    "deathmoth" to Tuning(1,7), "hostile_faceling" to Tuning(2,7), "false_puddle" to Tuning(4,5),
    "paintings" to Tuning(1,5), "smiler" to Tuning(2,9), "skin-stealer" to Tuning(4,8),
    "predatory_window" to Tuning(6,6), "biological_pipeline" to Tuning(7,7), "wretch" to Tuning(2,8),
    "cable_mimic" to Tuning(5,8), "the_beast_of_level_5" to Tuning(8,9),
    "hotel_corpse_lure" to Tuning(5,7), "jeff_the_killer" to Tuning(4,9),
    "jane_the_killer" to Tuning(4,9), "slenderman" to Tuning(8,10)
  )

  private fun profileFor(state: GameState, entityKey: String): Profile? {
    val entity = GameplayCatalog.entity(entityKey) ?: return null
    val (maxHp, damage) = GameplayCatalog.entityStats(entity, state.levelRuntime.stageIndex)
    val combatTuning = tuning[entity.key] ?: Tuning(3, 7)
    return Profile(entity.key, entity.name, maxHp, damage, combatTuning.armor, combatTuning.aggression)
  }

  fun active(state: GameState): Snapshot? = decode(state)?.takeIf { it.phase == Phase.ACTIVE }

  fun start(state: GameState, entityKey: String): GameState {
    if (active(state) != null) return state
    val profile = profileFor(state, entityKey) ?: return state
    val projected = CharacterStatRules.project(state, state.party.leaderId)
    val playerMax = projected?.maxHp ?: state.metadata[PLAYER_MAX_HP]?.toIntOrNull()?.coerceAtLeast(1) ?: 50
    val playerHp = projected?.currentHp ?: state.metadata[PLAYER_HP]?.toIntOrNull()?.coerceIn(0, playerMax) ?: playerMax
    val seed = stableSeed(entityKey, state.turn.currentTurnId, state.time.elapsedSubjectiveMinutes)
    val snapshot = Snapshot(
      encounterId = "${state.turn.currentTurnId}:${entityKey}:${abs(seed)}",
      entityKey = entityKey,
      entityName = profile.displayName,
      phase = Phase.ACTIVE,
      playerHp = playerHp,
      playerMaxHp = playerMax,
      entityHp = profile.maxHp,
      entityMaxHp = profile.maxHp,
      entityCondition = EntityCondition.HEALTHY,
      range = RangeBand.NEAR,
      cover = Cover.EXPOSED,
      momentum = 0,
      opening = 0,
      escapeProgress = 0,
      noise = 0,
      telegraph = telegraphFor(profile, seed, 0),
      telegraphRevealed = false,
      eventCounter = 0,
      seed = seed
    )
    var next = encode(state, snapshot)
    val initial = PokerDiceRuntime.ensureInitialRoll(diceSeed(seed), 0, PokerDiceRuntime.newState())
    next = writeRngSequence(next, initial.nextSequence)
    return writeDice(next, initial.state)
  }

  fun dice(state: GameState): PokerDiceState? {
    val snapshot = active(state) ?: return null
    return readDice(state, snapshot.seed)
  }

  fun setHold(state: GameState, dieIndex: Int, held: Boolean): GameState {
    val current = dice(state) ?: throw IllegalStateException("No active combat")
    return writeDice(state, PokerDiceRuntime.setHold(current, dieIndex, held))
  }

  fun rerollDice(state: GameState): GameState {
    val snapshot = active(state) ?: throw IllegalStateException("No active combat")
    val current = readDice(state, snapshot.seed)
    val rolled = PokerDiceRuntime.roll(diceSeed(snapshot.seed), readRngSequence(state), current)
    return writeDice(writeRngSequence(state, rolled.nextSequence), rolled.state)
  }

  fun finishHand(state: GameState): GameState {
    val current = dice(state) ?: throw IllegalStateException("No active combat")
    return writeDice(state, PokerDiceRuntime.finish(current))
  }

  fun resolveFinalizedHand(state: GameState): Resolution {
    val dice = dice(state) ?: return Resolution(state, handled = false)
    if (!dice.finalized) throw IllegalStateException("Hand chưa được chốt.")
    if (dice.resolved) return Resolution(state, handled = true)

    val resolving = writeDice(state, PokerDiceRuntime.markResolved(dice))
    val result = resolve(resolving, "EXECUTE", "poker-dice attack")
    if (!result.handled || result.entityDestroyed || result.escaped) return result

    val nextSnapshot = active(result.state)
    val next = if (nextSnapshot != null) {
      val prepared = PokerDiceRuntime.ensureInitialRoll(
        diceSeed(nextSnapshot.seed),
        readRngSequence(result.state),
        PokerDiceRuntime.newState()
      )
      writeDice(writeRngSequence(result.state, prepared.nextSequence), prepared.state)
    } else result.state
    return result.copy(state = next)
  }

  fun resolve(state: GameState, actionKind: String, action: String): Resolution {
    val current = active(state) ?: return Resolution(state, handled = false)
    val profile = profileFor(state, current.entityKey) ?: return Resolution(clear(state), handled = false)
    val intent = classify(actionKind, action)
    var c = current.copy(eventCounter = current.eventCounter + 1)
    val log = mutableListOf<String>()

    when (intent) {
      Intent.READ -> {
        c = c.copy(
          telegraphRevealed = true,
          opening = min(3, c.opening + 1),
          momentum = min(3, c.momentum + 1)
        )
        log += "Kai đọc được nhịp tấn công của ${c.entityName}; sơ hở tăng lên."
      }
      Intent.EVADE -> {
        val goodCounter = c.telegraph in setOf("LUNGE", "GRAB", "RUSH")
        c = c.copy(
          range = if (c.range == RangeBand.CLOSE) RangeBand.NEAR else c.range,
          momentum = (c.momentum + if (goodCounter) 2 else 1).coerceIn(-3, 3),
          opening = min(3, c.opening + if (goodCounter) 2 else 1),
          escapeProgress = min(100, c.escapeProgress + if (goodCounter) 18 else 10),
          cover = if (c.cover == Cover.EXPOSED) Cover.PARTIAL else c.cover
        )
        log += if (goodCounter) "Kai né đúng telegraph, cướp thế chủ động." else "Kai đổi góc và giảm áp lực trực diện."
      }
      Intent.MOVE -> {
        val nextRange = when (c.range) {
          RangeBand.CLOSE -> RangeBand.NEAR
          RangeBand.NEAR -> RangeBand.FAR
          RangeBand.FAR -> RangeBand.FAR
        }
        c = c.copy(
          range = nextRange,
          cover = if (c.cover == Cover.EXPOSED) Cover.PARTIAL else Cover.HARD,
          escapeProgress = min(100, c.escapeProgress + 15),
          momentum = min(3, c.momentum + 1)
        )
        log += "Kai tái định vị, kéo giãn khoảng cách và tìm vật che chắn."
      }
      Intent.GUARD -> {
        c = c.copy(cover = Cover.HARD, momentum = min(3, c.momentum + 1), opening = min(3, c.opening + 1))
        log += "Kai khóa tư thế phòng thủ và ép ${c.entityName} phải lộ hướng tấn công."
      }
      Intent.ESCAPE -> {
        val gain = 20 + c.momentum.coerceAtLeast(0) * 5 + when (c.cover) { Cover.HARD -> 15; Cover.PARTIAL -> 8; Cover.EXPOSED -> 0 }
        c = c.copy(escapeProgress = min(100, c.escapeProgress + gain), momentum = min(3, c.momentum + 1))
        log += "Kai dồn ưu thế vào đường thoát (${c.escapeProgress}%)."
      }
      Intent.ATTACK -> {
        val finalizedDice = dice(state)?.takeIf { it.finalized }
        if (finalizedDice != null) {
          val actorId = state.party.leaderId
          val stats = CharacterStatRules.project(state, actorId)
          val hand = PokerDiceRules.fromV2Name(finalizedDice.hand)
          val baseAttack = state.characters[actorId]?.metadata?.get("baseAttack")?.toIntOrNull()?.coerceAtLeast(1) ?: 30
          val str = stats?.stats?.get(com.rabpit.backroom.core.gameplay.CharacterStat.STR)?.effective ?: 5
          val skl = stats?.stats?.get(com.rabpit.backroom.core.gameplay.CharacterStat.SKL)?.effective ?: 5
          val skills = GameplayCatalog.activeSkills(actorId)
          val ultimate = GameplayCatalog.ultimate(actorId)
          val actionName: String
          val rawDamage = when (hand) {
            PokerDiceRules.Hand.NO_HAND, PokerDiceRules.Hand.ONE_PAIR, PokerDiceRules.Hand.TWO_PAIR -> {
              actionName = if (hand == PokerDiceRules.Hand.TWO_PAIR) "né và phản công" else "đánh thường"
              PokerDiceRules.basicDamage(baseAttack, str, PokerDiceRules.basicHandPercent(hand))
            }
            PokerDiceRules.Hand.SSF, PokerDiceRules.Hand.FSF -> {
              if (ultimate == null) {
                actionName = "Ultimate chưa được định nghĩa"
                0
              } else {
                actionName = ultimate.name
                val currentDamage = PokerDiceRules.basicDamage(baseAttack, str, 100)
                PokerDiceRules.ultimateDamage(
                  currentDamage, ultimate.hitCount, ultimate.bonusPercent, PokerDiceRules.ultimateHandPercent(hand)
                )
              }
            }
            else -> {
              val skill = skills.getOrNull(positiveMod(mix(c.seed, c.eventCounter + 73), skills.size.coerceAtLeast(1)))
              if (skill == null) {
                actionName = "Skill chưa được định nghĩa"
                0
              } else {
                actionName = skill.name
                PokerDiceRules.skillDamage(baseAttack, skill.damagePercent, skl, PokerDiceRules.skillHandPercent(hand))
              }
            }
          }
          val damage = max(0, rawDamage - if (rawDamage > 0) profile.armor else 0)
          val hp = max(0, c.entityHp - damage)
          c = c.copy(
            entityHp = hp,
            entityCondition = condition(hp, c.entityMaxHp),
            momentum = min(3, c.momentum + if (damage > 0) 1 else 0),
            opening = max(0, c.opening - 1),
            noise = min(100, c.noise + 35)
          )
          log += "${hand.token} $actionName: -$damage HP (${c.entityHp}/${c.entityMaxHp})."
        } else {
          val roll = roll(c, 100)
          val rangeBonus = when (c.range) { RangeBand.CLOSE -> 18; RangeBand.NEAR -> 10; RangeBand.FAR -> -5 }
          val hitChance = (58 + rangeBonus + c.opening * 11 + c.momentum * 6).coerceIn(20, 96)
          if (roll < hitChance) {
            val variance = 4 + roll(c.copy(eventCounter = c.eventCounter + 17), 9)
            val base = 18 + variance + c.opening * 7 + max(0, c.momentum) * 3
            val damage = max(1, base - profile.armor)
            val hp = max(0, c.entityHp - damage)
            c = c.copy(
              entityHp = hp,
              entityCondition = condition(hp, c.entityMaxHp),
              momentum = min(3, c.momentum + 1),
              opening = max(0, c.opening - 1),
              noise = min(100, c.noise + 35)
            )
            log += "Đòn đánh trúng ${c.entityName}: -$damage HP (${c.entityHp}/${c.entityMaxHp})."
          } else {
            c = c.copy(momentum = max(-3, c.momentum - 1), opening = max(0, c.opening - 1), noise = min(100, c.noise + 28))
            log += "Đòn đánh trượt; ${c.entityName} giành lại áp lực."
          }
        }
      }
      Intent.OTHER -> {
        c = c.copy(momentum = max(-3, c.momentum - 1))
        log += "Hành động không tạo được lợi thế chiến đấu rõ ràng."
      }
    }

    if (c.entityHp <= 0) {
      val persisted = encode(state, c.copy(phase = Phase.RESOLVED, entityCondition = EntityCondition.DESTROYED))
      val rewarded = ExplorationRuntime.entityVictoryRewards(persisted, c.entityKey)
      val cleared = clearCombatOnly(rewarded)
      return Resolution(cleared, true, log.joinToString(" ") + " ${c.entityName} đã bị tiêu diệt.", entityDestroyed = true)
    }
    if (c.escapeProgress >= 100) {
      val persisted = encode(state, c.copy(phase = Phase.RESOLVED))
      val cleared = clearCombatOnly(persisted)
      return Resolution(cleared, true, log.joinToString(" ") + " Kai cắt được truy đuổi và thoát khỏi encounter.", escaped = true)
    }

    // Enemy response. READ/guard/evasion reduce expected incoming damage; attacking blindly is riskier.
    val incomingRoll = roll(c.copy(eventCounter = c.eventCounter + 31), 100)
    val defense = when (intent) { Intent.EVADE -> 34; Intent.GUARD -> 30; Intent.MOVE -> 18; Intent.READ -> 12; else -> 0 } +
      when (c.cover) { Cover.HARD -> 22; Cover.PARTIAL -> 10; Cover.EXPOSED -> 0 } + max(0, c.momentum) * 4
    val enemyChance = (profile.aggression * 8 - defense + max(0, -c.momentum) * 7).coerceIn(8, 88)
    if (incomingRoll < enemyChance) {
      val damage = max(1, profile.attack + roll(c.copy(eventCounter = c.eventCounter + 47), 7) - when (c.cover) { Cover.HARD -> 8; Cover.PARTIAL -> 4; Cover.EXPOSED -> 0 })
      val hp = max(0, c.playerHp - damage)
      c = c.copy(playerHp = hp, momentum = max(-3, c.momentum - 1))
      log += "${c.entityName} phản công: Kai -$damage HP (${c.playerHp}/${c.playerMaxHp})."
    } else {
      log += "${c.entityName} không xuyên được thế phòng thủ/di chuyển của Kai."
    }

    c = c.copy(
      telegraph = telegraphFor(profile, c.seed, c.eventCounter),
      telegraphRevealed = false,
      opening = max(0, c.opening - if (intent == Intent.READ) 0 else 1)
    )
    var next = encode(state, c)
    next = CharacterProgressionRules.setCurrentHp(next, next.party.leaderId, c.playerHp)
    return Resolution(next, true, log.joinToString(" "))
  }

  fun toJson(state: GameState): JSONObject? = decode(state)?.let { c -> JSONObject().apply {
    put("active", c.phase == Phase.ACTIVE)
    put("encounterId", c.encounterId)
    put("entityKey", c.entityKey)
    put("entityName", c.entityName)
    put("playerHp", c.playerHp); put("playerMaxHp", c.playerMaxHp)
    put("entityHp", c.entityHp); put("entityMaxHp", c.entityMaxHp)
    put("entityCondition", c.entityCondition.name)
    put("range", c.range.name); put("cover", c.cover.name)
    put("momentum", c.momentum); put("opening", c.opening)
    put("escapeProgress", c.escapeProgress); put("noise", c.noise)
    put("telegraph", if (c.telegraphRevealed) c.telegraph else "UNKNOWN")
    put("telegraphRevealed", c.telegraphRevealed)
    readDice(state, c.seed).let { dice ->
      put("rngSequence", readRngSequence(state))
      put("diceState", JSONObject().apply {
        put("values", org.json.JSONArray(dice.values))
        put("held", org.json.JSONArray(dice.held))
        put("hasRolled", dice.hasRolled)
        put("rerollsUsed", dice.rerollsUsed)
        put("maxRerolls", dice.maxRerolls)
        put("finalized", dice.finalized)
        put("resolved", dice.resolved)
        put("hand", dice.hand)
      })
    }
  } }

  fun clear(state: GameState): GameState = clearCombatOnly(state)

  private fun encode(state: GameState, c: Snapshot): GameState {
    val metadata = state.metadata.toMutableMap()
    metadata["${PREFIX}encounterId"] = c.encounterId
    metadata["${PREFIX}entityKey"] = c.entityKey
    metadata["${PREFIX}entityName"] = c.entityName
    metadata["${PREFIX}phase"] = c.phase.name
    metadata[PLAYER_HP] = c.playerHp.toString()
    metadata[PLAYER_MAX_HP] = c.playerMaxHp.toString()
    metadata["${PREFIX}entityHp"] = c.entityHp.toString()
    metadata["${PREFIX}entityMaxHp"] = c.entityMaxHp.toString()
    metadata["${PREFIX}entityCondition"] = c.entityCondition.name
    metadata["${PREFIX}range"] = c.range.name
    metadata["${PREFIX}cover"] = c.cover.name
    metadata["${PREFIX}momentum"] = c.momentum.toString()
    metadata["${PREFIX}opening"] = c.opening.toString()
    metadata["${PREFIX}escapeProgress"] = c.escapeProgress.toString()
    metadata["${PREFIX}noise"] = c.noise.toString()
    metadata["${PREFIX}telegraph"] = c.telegraph
    metadata["${PREFIX}telegraphRevealed"] = c.telegraphRevealed.toString()
    metadata["${PREFIX}eventCounter"] = c.eventCounter.toString()
    metadata["${PREFIX}seed"] = c.seed.toString()
    return state.copy(metadata = metadata)
  }

  private fun decode(state: GameState): Snapshot? {
    val m = state.metadata
    val key = m["${PREFIX}entityKey"]?.takeIf { it.isNotBlank() } ?: return null
    val profile = profileFor(state, key) ?: return null
    val maxHp = m["${PREFIX}entityMaxHp"]?.toIntOrNull()?.coerceAtLeast(1) ?: profile.maxHp
    val hp = m["${PREFIX}entityHp"]?.toIntOrNull()?.coerceIn(0, maxHp) ?: maxHp
    val projected = CharacterStatRules.project(state, state.party.leaderId)
    val playerMax = m[PLAYER_MAX_HP]?.toIntOrNull()?.coerceAtLeast(1) ?: projected?.maxHp ?: 50
    return Snapshot(
      encounterId = m["${PREFIX}encounterId"].orEmpty(),
      entityKey = key,
      entityName = m["${PREFIX}entityName"] ?: profile.displayName,
      phase = enumOr(Phase.ACTIVE, m["${PREFIX}phase"]),
      playerHp = m[PLAYER_HP]?.toIntOrNull()?.coerceIn(0, playerMax) ?: projected?.currentHp ?: playerMax,
      playerMaxHp = playerMax,
      entityHp = hp,
      entityMaxHp = maxHp,
      entityCondition = condition(hp, maxHp),
      range = enumOr(RangeBand.NEAR, m["${PREFIX}range"]),
      cover = enumOr(Cover.EXPOSED, m["${PREFIX}cover"]),
      momentum = m["${PREFIX}momentum"]?.toIntOrNull()?.coerceIn(-3, 3) ?: 0,
      opening = m["${PREFIX}opening"]?.toIntOrNull()?.coerceIn(0, 3) ?: 0,
      escapeProgress = m["${PREFIX}escapeProgress"]?.toIntOrNull()?.coerceIn(0, 100) ?: 0,
      noise = m["${PREFIX}noise"]?.toIntOrNull()?.coerceIn(0, 100) ?: 0,
      telegraph = m["${PREFIX}telegraph"] ?: telegraphFor(profile, stableSeed(key, state.turn.currentTurnId, state.time.elapsedSubjectiveMinutes), 0),
      telegraphRevealed = m["${PREFIX}telegraphRevealed"].toBoolean(),
      eventCounter = m["${PREFIX}eventCounter"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
      seed = m["${PREFIX}seed"]?.toLongOrNull() ?: stableSeed(key, state.turn.currentTurnId, state.time.elapsedSubjectiveMinutes)
    )
  }

  private fun writeDice(state: GameState, dice: PokerDiceState): GameState {
    val metadata = state.metadata.toMutableMap()
    metadata["${PREFIX}dice.values"] = dice.values.joinToString(",")
    metadata["${PREFIX}dice.held"] = dice.held.joinToString(",") { if (it) "1" else "0" }
    metadata["${PREFIX}dice.hasRolled"] = dice.hasRolled.toString()
    metadata["${PREFIX}dice.rerollsUsed"] = dice.rerollsUsed.toString()
    metadata["${PREFIX}dice.maxRerolls"] = dice.maxRerolls.toString()
    metadata["${PREFIX}dice.finalized"] = dice.finalized.toString()
    metadata["${PREFIX}dice.resolved"] = dice.resolved.toString()
    metadata["${PREFIX}dice.hand"] = dice.hand
    return state.copy(metadata = metadata)
  }

  private fun readDice(state: GameState, seed: Long): PokerDiceState {
    val metadata = state.metadata
    val values = metadata["${PREFIX}dice.values"]?.split(',')?.mapNotNull(String::toIntOrNull)
      ?: throw IllegalStateException("Combat dice state bị thiếu.")
    val held = metadata["${PREFIX}dice.held"]?.split(',')?.map { it == "1" }
      ?: throw IllegalStateException("Combat dice state bị thiếu.")
    val hasRolled = metadata["${PREFIX}dice.hasRolled"].toBoolean()
    if (values.size != PokerDiceRules.DICE_COUNT || held.size != PokerDiceRules.DICE_COUNT) {
      throw IllegalStateException("Combat dice state không hợp lệ.")
    }
    if (values.any { it !in 0..6 } || (hasRolled && values.any { it !in 1..6 })) {
      throw IllegalStateException("Combat dice state không hợp lệ.")
    }
    return PokerDiceState(
      values = values,
      held = held,
      hasRolled = hasRolled,
      rerollsUsed = metadata["${PREFIX}dice.rerollsUsed"]?.toIntOrNull()?.coerceIn(0, PokerDiceRules.MAX_REROLLS) ?: 0,
      maxRerolls = PokerDiceRules.MAX_REROLLS,
      finalized = metadata["${PREFIX}dice.finalized"].toBoolean(),
      resolved = metadata["${PREFIX}dice.resolved"].toBoolean(),
      hand = metadata["${PREFIX}dice.hand"].orEmpty()
    )
  }

  private fun readRngSequence(state: GameState): Int =
    state.metadata["${PREFIX}rngSequence"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0

  private fun writeRngSequence(state: GameState, sequence: Int): GameState =
    state.copy(metadata = state.metadata + ("${PREFIX}rngSequence" to sequence.coerceAtLeast(0).toString()))

  private fun diceSeed(seed: Long): Int = (seed xor (seed ushr 32)).toInt().let { if (it == Int.MIN_VALUE) 1 else kotlin.math.abs(it) }

  private fun clearCombatOnly(state: GameState): GameState {
    val preservedHp = state.metadata[PLAYER_HP]
    val preservedMax = state.metadata[PLAYER_MAX_HP]
    val metadata = state.metadata.filterKeys { !it.startsWith(PREFIX) }.toMutableMap()
    if (preservedHp != null) metadata[PLAYER_HP] = preservedHp
    if (preservedMax != null) metadata[PLAYER_MAX_HP] = preservedMax
    return state.copy(metadata = metadata)
  }

  private fun classify(actionKind: String, raw: String): Intent {
    val text = raw.lowercase()
    if (containsAny(text, "bắn", "đánh", "chém", "đâm", "tấn công", "shoot", "attack", "fire")) return Intent.ATTACK
    if (containsAny(text, "né", "lách", "dodge", "evade", "tránh")) return Intent.EVADE
    if (containsAny(text, "chạy thoát", "bỏ chạy", "thoát", "escape", "flee")) return Intent.ESCAPE
    if (containsAny(text, "thủ", "đỡ", "chặn", "guard", "block", "cover")) return Intent.GUARD
    if (actionKind.equals("SEARCH", true) || containsAny(text, "quan sát", "đọc", "nhìn kỹ", "theo dõi", "observe", "read")) return Intent.READ
    if (actionKind.equals("EXPLORE", true) || containsAny(text, "lùi", "tiến", "di chuyển", "núp", "vòng", "move", "reposition")) return Intent.MOVE
    return Intent.OTHER
  }

  private fun containsAny(text: String, vararg needles: String) = needles.any(text::contains)

  private fun condition(hp: Int, maxHp: Int): EntityCondition {
    if (hp <= 0) return EntityCondition.DESTROYED
    val ratio = hp.toDouble() / maxHp.toDouble()
    return when {
      ratio > .75 -> EntityCondition.HEALTHY
      ratio > .50 -> EntityCondition.HURT
      ratio > .25 -> EntityCondition.WOUNDED
      else -> EntityCondition.CRITICAL
    }
  }

  private fun telegraphFor(profile: Profile, seed: Long, counter: Int): String {
    val options = when {
      profile.key == "smiler" -> listOf("STALK", "RUSH", "VANISH")
      profile.key == "cable_mimic" -> listOf("GRAB", "LUNGE", "FLANK")
      profile.key == "slenderman" -> listOf("STALK", "GRAB", "RUSH")
      else -> listOf("LUNGE", "GRAB", "RUSH", "FLANK")
    }
    return options[positiveMod(mix(seed, counter), options.size)]
  }

  private fun roll(c: Snapshot, bound: Int): Int = positiveMod(mix(c.seed, c.eventCounter), bound)
  private fun stableSeed(entityKey: String, turnId: String, time: Long): Long = mix(entityKey.hashCode().toLong() * 31L + turnId.hashCode(), time.toInt())
  private fun mix(seed: Long, counter: Int): Long {
    var x = seed xor (counter.toLong() * -7046029254386353131L)
    x = (x xor (x ushr 30)) * -4658895280553007687L
    x = (x xor (x ushr 27)) * -7723592293110705685L
    return x xor (x ushr 31)
  }
  private fun positiveMod(value: Long, bound: Int): Int = ((value and Long.MAX_VALUE) % bound.toLong()).toInt()
  private inline fun <reified T : Enum<T>> enumOr(fallback: T, raw: String?): T = enumValues<T>().firstOrNull { it.name == raw } ?: fallback
}
