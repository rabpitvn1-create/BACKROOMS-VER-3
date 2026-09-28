package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class InventoryPolicyTest {
  private fun stateWith(vararg characters: CharacterState): GameState {
    val all = listOf(CharacterState(KAI_ID, "Player")) + characters
    return GameState.initial().copy(
      characters = all.associateBy { it.id },
      inventories = all.associate { it.id to InventoryState(it.id) },
      equipment = all.associate { it.id to EquipmentState(it.id) }
    )
  }

  @Test fun profilesComeFromRoleOrMetadata() {
    val state = stateWith(
      CharacterState("special", "Special", metadata = mapOf("inventoryProfile" to "special")),
      CharacterState("normal", "Normal")
    )
    assertEquals(InventoryProfile(9, 999), InventoryPolicy.profileFor(state, KAI_ID))
    assertEquals(InventoryProfile(4, 20), InventoryPolicy.profileFor(state, "special"))
    assertEquals(InventoryProfile(2, 2), InventoryPolicy.profileFor(state, "normal"))
  }

  @Test fun leaderLimitsAreEnforced() {
    val items = (1..9).associate { "i$it" to ItemStack("i$it", "Item $it", 1) }
    val state = stateWith().copy(inventories = mapOf(KAI_ID to InventoryState(KAI_ID, items)))
    assertEquals(
      "inventory_slot_limit",
      InventoryPolicy.validateAddition(state, KAI_ID, state.inventories.getValue(KAI_ID), ItemStack("i10", "Item 10"), 1)
    )
  }

  @Test fun equippedItemDoesNotConsumeBackpackTypeSlot() {
    val items = (1..9).associate { "i$it" to ItemStack("i$it", "Item $it", 1) }
    val state = stateWith().copy(
      inventories = mapOf(KAI_ID to InventoryState(KAI_ID, items)),
      equipment = mapOf(KAI_ID to EquipmentState(KAI_ID, mapOf("weapon" to "i1")))
    )
    assertNull(
      InventoryPolicy.validateAddition(state, KAI_ID, state.inventories.getValue(KAI_ID), ItemStack("i10", "Item 10"), 1)
    )
  }
}
