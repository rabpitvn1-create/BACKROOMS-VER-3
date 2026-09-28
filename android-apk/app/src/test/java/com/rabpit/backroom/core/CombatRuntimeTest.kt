package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.GameplayCatalog
import com.rabpit.backroom.core.gameplay.PokerDiceRules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CombatRuntimeTest {
  @Test fun entityTriggerStartsOneAuthoritativeEncounterWithHealth() {
    val started = CombatRuntime.start(GameState.initial(), "hound")
    val combat = CombatRuntime.active(started)
    assertNotNull(combat)
    assertEquals("hound", combat!!.entityKey)
    assertEquals(150, combat.entityMaxHp)
    assertEquals(150, combat.entityHp)
    assertEquals(545, combat.playerMaxHp)
    assertEquals(50, combat.playerHp)

    val duplicate = CombatRuntime.start(started, "smiler")
    assertEquals("hound", CombatRuntime.active(duplicate)!!.entityKey)
  }

  @Test fun repeatedAuthoritativeAttacksEventuallyDestroyAndClearEntity() {
    var state = CombatRuntime.start(GameState.initial(), "hound")
    var destroyed = false
    repeat(24) {
      if (destroyed) return@repeat
      val result = CombatRuntime.resolve(state, "EXECUTE", "bắn Hound bằng Magnum")
      assertTrue(result.handled)
      state = result.state
      destroyed = result.entityDestroyed
    }
    assertTrue("Entity must be destroyable by authoritative combat resolution", destroyed)
    assertNull(CombatRuntime.active(state))
  }

  @Test fun combatExploreIsMovementNotAnotherEncounter() {
    val started = CombatRuntime.start(GameState.initial(), "skin-stealer")
    val before = CombatRuntime.active(started)!!
    val result = CombatRuntime.resolve(started, "EXPLORE", "lùi lại tìm vật che chắn")
    assertTrue(result.handled)
    val after = CombatRuntime.active(result.state)
    if (after != null) {
      assertEquals("skin-stealer", after.entityKey)
      assertTrue(after.escapeProgress >= before.escapeProgress)
      assertTrue(after.range.ordinal >= before.range.ordinal)
    }
  }

  @Test fun escapeResolutionClearsEncounterWithoutDestroyingRequirement() {
    var state = CombatRuntime.start(GameState.initial(), "smiler")
    var escaped = false
    repeat(12) {
      if (escaped) return@repeat
      val move = CombatRuntime.resolve(state, "EXPLORE", "lùi vào cover và di chuyển")
      state = move.state
      if (move.escaped) { escaped = true; return@repeat }
      val flee = CombatRuntime.resolve(state, "EXECUTE", "chạy thoát khỏi encounter")
      state = flee.state
      escaped = flee.escaped
    }
    assertTrue(escaped)
    assertNull(CombatRuntime.active(state))
  }

  @Test fun readActionRevealsTelegraphAndBuildsOpeningWhenEncounterSurvives() {
    val state = CombatRuntime.start(GameState.initial(), "clump")
    val result = CombatRuntime.resolve(state, "SEARCH", "quan sát kỹ chuyển động của nó")
    assertTrue(result.handled)
    val after = CombatRuntime.active(result.state)
    assertNotNull(after)
    assertTrue(after!!.opening >= 1)
    assertTrue(after.momentum >= 0)
    assertFalse(after.telegraph.isBlank())
  }

  @Test fun pokerDiceMatchesV2StateAndRerollContract() {
    val fresh = com.rabpit.backroom.core.gameplay.PokerDiceRuntime.newState()
    assertEquals(listOf(0, 0, 0, 0, 0), fresh.values)
    assertFalse(fresh.hasRolled)
    assertEquals("", fresh.hand)
    assertEquals(0, fresh.rerollsUsed)
    assertEquals(3, fresh.maxRerolls)

    var state = CombatRuntime.start(GameState.initial(), "hound")
    val initial = CombatRuntime.dice(state)!!
    assertTrue(initial.hasRolled)
    assertEquals(0, initial.rerollsUsed)
    assertTrue(initial.values.all { it in 1..6 })
    assertEquals(PokerDiceRules.classify(*initial.values.toIntArray()).v2Name, initial.hand)

    state = CombatRuntime.setHold(state, 0, true)
    val heldValue = CombatRuntime.dice(state)!!.values[0]
    repeat(3) {
      for (index in 1 until PokerDiceRules.DICE_COUNT) {
        state = CombatRuntime.setHold(state, index, false)
      }
      state = CombatRuntime.rerollDice(state)
    }
    val rerolled = CombatRuntime.dice(state)!!
    assertEquals(3, rerolled.rerollsUsed)
    assertEquals(heldValue, rerolled.values[0])
    assertEquals(rerolled, CombatRuntime.dice(CombatRuntime.rerollDice(state))!!)

    state = CombatRuntime.finishHand(state)
    val finalized = CombatRuntime.dice(state)!!
    assertTrue(finalized.finalized)
    assertEquals(PokerDiceRules.classify(*finalized.values.toIntArray()).v2Name, finalized.hand)

    val combat = CombatRuntime.toJson(state)!!
    assertTrue(combat.has("rngSequence"))
    assertFalse(combat.has("dice"))
    val json = combat.getJSONObject("diceState")
    assertEquals(
      setOf("values", "held", "hasRolled", "rerollsUsed", "maxRerolls", "finalized", "resolved", "hand"),
      json.keys().asSequence().toSet()
    )
    assertTrue(json.getBoolean("hasRolled"))
    assertEquals(3, json.getInt("maxRerolls"))
    assertEquals(finalized.hand, json.getString("hand"))
  }
  @Test fun combatCyclesPartyActorsAndProjectsOverlayParticipants() {
    val base = GameState.initial()
    val iris = CharacterState("iris", "Iris", metadata = mapOf("baseAttack" to "28"))
    val partyState = base.copy(
      characters = base.characters + ("iris" to iris),
      party = PartyState(leaderId = PLAYER_ID, memberIds = listOf(PLAYER_ID, "iris"))
    )
    val started = CombatRuntime.start(partyState, "hound")
    val before = CombatRuntime.toJson(started)!!
    assertEquals("Cao Minh", before.getString("currentActor"))
    assertEquals(0, before.getInt("actorIndex"))
    assertEquals(2, before.getJSONArray("participants").length())

    val resolved = CombatRuntime.resolve(started, "EXECUTE", "đánh Hound")
    val after = CombatRuntime.toJson(resolved.state)!!
    assertEquals("Iris", after.getString("currentActor"))
    assertEquals(1, after.getInt("actorIndex"))
  }

  @Test fun entityResponseUsesV2IndependentSkillPool() {
    val skillNames = GameplayCatalog.entity("hound")!!.skills.map { it.name }
    var observed = false
    repeat(64) { index ->
      val base = GameState.initial().copy(turn = TurnState(currentTurnId = "TURN_${index + 1}"))
      val result = CombatRuntime.resolve(CombatRuntime.start(base, "hound"), "EXECUTE", "đánh Hound")
      if (skillNames.any(result.reply::contains)) observed = true
    }
    assertTrue("At least one deterministic Hound response should proc a V2 Entity skill", observed)
  }

}
