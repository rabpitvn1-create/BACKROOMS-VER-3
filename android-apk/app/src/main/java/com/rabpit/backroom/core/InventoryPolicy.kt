package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.EquipmentRules

data class InventoryProfile(val maxTypes: Int, val maxPerType: Int)

object InventoryPolicy {
  val LEADER = InventoryProfile(maxTypes = 9, maxPerType = 999)
  val SPECIAL = InventoryProfile(maxTypes = 6, maxPerType = 20)
  val NORMAL = InventoryProfile(maxTypes = 2, maxPerType = 2)

  fun profileFor(state: GameState, characterId: String): InventoryProfile =
    when (state.characters[characterId]?.metadata?.get("inventoryProfile")?.lowercase()) {
      "special" -> SPECIAL
      "normal" -> NORMAL
      "leader" -> LEADER
      else -> if (characterId == state.party.leaderId) LEADER else NORMAL
    }

  fun validateAddition(
    state: GameState,
    ownerId: String,
    inventory: InventoryState,
    item: ItemStack,
    quantity: Int
  ): String? {
    if (quantity <= 0) return "quantity_must_be_positive"
    val normalized = ItemContentRules.normalize(item)
    val profile = profileFor(state, ownerId)
    val old = inventory.items[normalized.itemId]
    if ((old?.quantity ?: 0) + quantity > profile.maxPerType) return "inventory_stack_limit"
    val carriedTypes = inventory.items.keys.count { !EquipmentRules.isEquipped(state, ownerId, it) }
    if (old == null && carriedTypes >= profile.maxTypes) return "inventory_slot_limit"
    return null
  }
}
