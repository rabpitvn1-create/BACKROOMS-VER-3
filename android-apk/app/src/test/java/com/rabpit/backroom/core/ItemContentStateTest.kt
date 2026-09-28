package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class ItemContentStateTest {
  private fun grant(name: String, id: String = "raw", quantity: Int = 1) = ItemCommand(
    "grant-$id-$name", "TURN_1", KAI_ID, source = CommandSource.SYSTEM,
    operation = ItemCommand.Operation.PICKUP, itemId = id, itemName = name, quantity = quantity
  )

  private fun use(id: String, n: Int) = ItemCommand(
    "use-$n-$id", "TURN_1", KAI_ID, source = CommandSource.RULE,
    operation = ItemCommand.Operation.USE, itemId = id, itemName = id
  )

  @Test fun waterBottleUsesThreeDiscreteStates() {
    val full = StateReducer.execute(GameState.initial(), grant("Chai nước", "water")).state
    val fullId = "water-bottle:full"
    assertEquals(ContentState.FULL, full.inventories.getValue(KAI_ID).items.getValue(fullId).contentState)

    val low = StateReducer.execute(full, use(fullId, 1))
    val lowId = "water-bottle:low"
    assertTrue(low.applied)
    assertEquals(ContentState.LOW, low.state.inventories.getValue(KAI_ID).items.getValue(lowId).contentState)

    val empty = StateReducer.execute(low.state, use(lowId, 2))
    val emptyId = "water-bottle:empty"
    assertTrue(empty.applied)
    assertEquals(ContentState.EMPTY, empty.state.inventories.getValue(KAI_ID).items.getValue(emptyId).contentState)

    val rejected = StateReducer.execute(empty.state, use(emptyId, 3))
    assertFalse(rejected.applied)
    assertEquals("item_content_empty", rejected.validation.reason)
  }

  @Test fun usingOneStackMemberSplitsStateInsteadOfInventingAmounts() {
    val full = StateReducer.execute(GameState.initial(), grant("Chai nước", "water", 3)).state
    val used = StateReducer.execute(full, use("water-bottle:full", 1))
    assertEquals(2, used.state.inventories.getValue(KAI_ID).items.getValue("water-bottle:full").quantity)
    assertEquals(1, used.state.inventories.getValue(KAI_ID).items.getValue("water-bottle:low").quantity)
  }

  @Test fun preciseAmountsAreForbidden() {
    val invalid = StateReducer.execute(GameState.initial(), grant("Chai nước 200ml", "water-200"))
    assertFalse(invalid.applied)
    assertEquals("precise_content_amount_forbidden", invalid.validation.reason)
  }

  @Test fun emptyContainersRemainEmpty() {
    assertEquals(ContentState.EMPTY, ItemContentRules.normalize(ItemStack("food", "Vỏ thức ăn rỗng")).contentState)
    assertEquals(ContentState.EMPTY, ItemContentRules.normalize(ItemStack("box", "Vỏ hộp rỗng")).contentState)
    val casing = ItemContentRules.normalize(ItemStack("casing", "Vỏ đạn"))
    assertEquals(ContentState.EMPTY, casing.contentState)
    assertNull(ItemContentRules.nextAfterUse(casing))
  }
}
