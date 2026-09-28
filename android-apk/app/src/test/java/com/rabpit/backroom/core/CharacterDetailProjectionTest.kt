package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class CharacterDetailProjectionTest {
  @Test fun partyProjectionKeepsPartyOrderLeaderAndSubjectiveTime() {
    val player = CharacterState(KAI_ID, "Player", avatarRef = "avatars/player.png")
    val companion = CharacterState("companion", "Companion", avatarRef = "avatars/companion.png")
    val state = GameState.initial().copy(
      characters = linkedMapOf(KAI_ID to player, "companion" to companion),
      party = PartyState(leaderId = KAI_ID, memberIds = listOf(KAI_ID, "companion"), maxMembers = 4),
      time = GameTimeState(elapsedSubjectiveMinutes = 845L)
    )

    val projected = CharacterDetailProjector.projectParty(state)

    assertEquals(KAI_ID, projected.leaderId)
    assertEquals(4, projected.maxMembers)
    assertEquals(845L, projected.elapsedSubjectiveMinutes)
    assertEquals(listOf(KAI_ID, "companion"), projected.members.map { it.id })
    assertTrue(projected.members[0].isLeader)
    assertFalse(projected.members[1].isLeader)
  }

  @Test fun characterProjectionUsesCharacterOwnedInventoryEquipmentStatusesAndPhysiology() {
    val injury = StatusEffect("companion-injury", "INJURY", "event", persistent = true)
    val unrelated = StatusEffect("player-effect", "BUFF", "event")
    val companion = CharacterState(
      id = "companion",
      name = "Companion",
      avatarRef = "avatars/companion.png",
      healthState = "INJURED",
      injuries = listOf("left_arm_cut"),
      inventoryId = "companion-pack",
      equipmentId = "companion-kit",
      statusIds = setOf(injury.id),
      physiology = PhysiologyState(
        minutesSinceFood = 800L,
        minutesSinceWater = 400L,
        minutesAwake = 1300L,
        painState = "moderate"
      )
    )
    val state = GameState.initial().copy(
      characters = mapOf(KAI_ID to GameState.initial().characters.getValue(KAI_ID), "companion" to companion),
      party = PartyState(memberIds = listOf(KAI_ID, "companion")),
      inventories = mapOf(
        KAI_ID to InventoryState(KAI_ID, mapOf("player-item" to ItemStack("player-item", "Player Item"))),
        "companion-pack" to InventoryState("companion-pack", mapOf(
          "b" to ItemStack("b", "Zeta"),
          "a" to ItemStack("a", "Alpha")
        ))
      ),
      equipment = mapOf(
        KAI_ID to GameState.initial().equipment.getValue(KAI_ID),
        "companion-kit" to EquipmentState("companion-kit", mapOf("weapon" to "ivory", "armor" to "argus"))
      ),
      statuses = mapOf(injury.id to injury, unrelated.id to unrelated)
    )

    val projected = CharacterDetailProjector.projectCharacter(state, "companion")!!

    assertEquals("Companion", projected.name)
    assertEquals("INJURED", projected.healthState)
    assertEquals(listOf("left_arm_cut"), projected.injuries)
    assertEquals(listOf("Alpha", "Zeta"), projected.inventory.map { it.name })
    assertEquals(mapOf("armor" to "argus", "weapon" to "ivory"), projected.equipment)
    assertEquals(listOf(injury), projected.statusEffects)
    assertEquals(PhysiologyBand.MILD, projected.physiology.hunger)
    assertEquals(PhysiologyBand.MILD, projected.physiology.thirst)
    assertEquals(PhysiologyBand.MODERATE, projected.physiology.sleepDeprivation)
    assertEquals("moderate", projected.physiology.pain)
  }

  @Test fun projectionDoesNotInventMissingData() {
    val unknown = CharacterState("survivor", "Survivor")
    val state = GameState.initial().copy(
      characters = mapOf(KAI_ID to GameState.initial().characters.getValue(KAI_ID), "survivor" to unknown),
      party = PartyState(memberIds = listOf(KAI_ID, "survivor"))
    )

    val projected = CharacterDetailProjector.projectCharacter(state, "survivor")!!

    assertTrue(projected.inventory.isEmpty())
    assertTrue(projected.equipment.isEmpty())
    assertTrue(projected.statusEffects.isEmpty())
    assertEquals(PhysiologyBand.UNKNOWN, projected.physiology.hunger)
    assertEquals(PhysiologyBand.UNKNOWN, projected.physiology.thirst)
    assertEquals(PhysiologyBand.UNKNOWN, projected.physiology.sleepDeprivation)
    assertNull(projected.physiology.pain)
    assertNull(projected.physiology.infection)
    assertNull(projected.physiology.thermal)
  }

  @Test fun unknownCharacterProjectionReturnsNull() {
    assertNull(CharacterDetailProjector.projectCharacter(GameState.initial(), "missing"))
  }
}
