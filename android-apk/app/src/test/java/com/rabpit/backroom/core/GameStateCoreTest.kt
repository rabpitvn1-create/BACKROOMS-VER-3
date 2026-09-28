package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class GameStateCoreTest {
  private fun base(vararg characters: CharacterState): GameState {
    val all = listOf(CharacterState(PLAYER_ID, "Cao Minh")) + characters
    return GameState.initial().copy(
      characters = all.associateBy { it.id },
      inventories = all.associate { it.id to InventoryState(it.id) },
      equipment = all.associate { it.id to EquipmentState(it.id) }
    )
  }

  private fun item(
    id: String,
    op: ItemCommand.Operation,
    quantity: Int = 1,
    target: String? = null,
    slot: String? = null,
    source: CommandSource = CommandSource.SYSTEM
  ) = ItemCommand("cmd-$id-$op-$quantity-${target.orEmpty()}-$source", "TURN_1", PLAYER_ID, target, source, op, id, id, quantity, slot)

  @Test fun authoritativeGrantDropAndDuplicateAreDeterministic() {
    val picked = StateReducer.execute(base(), item("water", ItemCommand.Operation.PICKUP))
    assertEquals(1, picked.state.inventories.getValue(PLAYER_ID).items.getValue("water").quantity)
    assertTrue(StateReducer.execute(picked.state, item("water", ItemCommand.Operation.PICKUP)).duplicate)
    val dropped = StateReducer.execute(picked.state, item("water", ItemCommand.Operation.DROP))
    assertFalse(dropped.state.inventories.getValue(PLAYER_ID).items.containsKey("water"))
  }

  @Test fun playerPickupIsRejectedButAuthoritativeGrantIsAllowed() {
    val playerPickup = StateReducer.execute(base(), item("water", ItemCommand.Operation.PICKUP, source = CommandSource.RULE))
    assertFalse(playerPickup.applied)
    assertEquals("player_pickup_unavailable", playerPickup.validation.reason)
    assertTrue(StateReducer.execute(base(), item("water", ItemCommand.Operation.PICKUP)).applied)
  }

  @Test fun transferRequiresOwnershipAndKnownTarget() {
    val companion = CharacterState("companion", "Companion")
    val picked = StateReducer.execute(base(companion), item("water", ItemCommand.Operation.PICKUP, 2)).state
    val moved = StateReducer.execute(picked, item("water", ItemCommand.Operation.TRANSFER, 1, "companion"))
    assertTrue(moved.applied)
    assertEquals(1, moved.state.inventories.getValue(PLAYER_ID).items.getValue("water").quantity)
    assertEquals(1, moved.state.inventories.getValue("companion").items.getValue("water").quantity)
  }

  @Test fun equipAndUnequipUseOwnedItem() {
    val picked = StateReducer.execute(base(), item("gun", ItemCommand.Operation.PICKUP)).state
    val equipped = StateReducer.execute(picked, item("gun", ItemCommand.Operation.EQUIP, slot = "weapon"))
    assertEquals("gun", equipped.state.equipment.getValue(PLAYER_ID).slots["weapon"])
    val unequipped = StateReducer.execute(equipped.state, item("gun", ItemCommand.Operation.UNEQUIP, slot = "weapon"))
    assertNull(unequipped.state.equipment.getValue(PLAYER_ID).slots["weapon"])
  }

  @Test fun partyNeedsPresenceConsentAndHasFourMemberLimit() {
    val people = (1..4).map { CharacterState("p$it", "P$it") }
    var state = base(*people.toTypedArray())
    for (i in 1..3) {
      state = StateReducer.execute(state, PartyCommand(
        "join-$i", "TURN_1", PLAYER_ID, "p$i", CommandSource.UI,
        PartyCommand.Operation.ADD, true, true
      )).state
    }
    assertEquals(4, state.party.memberIds.size)
    val full = StateReducer.execute(state, PartyCommand(
      "join-4", "TURN_1", PLAYER_ID, "p4", CommandSource.UI,
      PartyCommand.Operation.ADD, true, true
    ))
    assertEquals("party_full", full.validation.reason)
  }

  @Test fun validatedWorldDeltaRequiresCoreValidation() {
    val rejected = StateReducer.execute(base(), ValidatedStateCommand(
      "world-invalid", "TURN_1", source = CommandSource.GEMINI,
      location = "Level 1", validatedByGameEngine = false
    ))
    assertEquals("engine_validation_required", rejected.validation.reason)

    val accepted = StateReducer.execute(base(), ValidatedStateCommand(
      "world-valid", "TURN_1", source = CommandSource.GEMINI,
      location = "Level 1", validatedByGameEngine = true
    ))
    assertEquals("Level 1", accepted.state.world["location"])
  }
}
