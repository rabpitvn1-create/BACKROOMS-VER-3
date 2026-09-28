package com.rabpit.backroom.core.gameplay

import com.rabpit.backroom.core.GameState
import com.rabpit.backroom.core.ItemStack

enum class EquipmentSlot(val key: String) {
  WEAPON("weapon"),
  ARMOR("armor"),
  HEAD("head"),
  GAUNTLETS("gauntlets"),
  GREAVES("greaves"),
  RING("ring"),
  SPECIAL("special"),
  OUTFIT("outfit"),
  FOOTWEAR("footwear");

  companion object {
    fun fromRaw(raw: String?): EquipmentSlot? = when (
      raw?.trim()?.lowercase()?.replace('-', '_')
    ) {
      "weapon", "weapon_primary", "weapon_secondary" -> WEAPON
      "armor" -> ARMOR
      "head", "helmet", "mask" -> HEAD
      "gauntlets", "gloves" -> GAUNTLETS
      "greaves", "boots" -> GREAVES
      "ring" -> RING
      "special" -> SPECIAL
      "outfit" -> OUTFIT
      "footwear" -> FOOTWEAR
      else -> null
    }
  }
}

data class EquipmentBonuses(
  val hp: Int = 0,
  val str: Int = 0,
  val def: Int = 0,
  val skl: Int = 0,
  val vit: Int = 0
) {
  operator fun plus(other: EquipmentBonuses) = EquipmentBonuses(
    hp = hp + other.hp,
    str = str + other.str,
    def = def + other.def,
    skl = skl + other.skl,
    vit = vit + other.vit
  )

  fun forStat(stat: CharacterStat): Int = when (stat) {
    CharacterStat.STR -> str
    CharacterStat.DEF -> def
    CharacterStat.SKL -> skl
    CharacterStat.VIT -> vit
  }
}

/**
 * Generic equipment mechanics only. Character-specific loadouts and canon data stay outside V3 gameplay.
 * EquipmentState stores references to the one owned ItemStack in InventoryState.
 */
object EquipmentRules {
  fun occupiedSlots(item: ItemStack, requestedSlot: String?): Set<EquipmentSlot> {
    val raw = item.metadata["equipmentSlots"] ?: item.metadata["equipmentSlot"]
    val declared = raw.orEmpty().split(',', ';', '|')
      .mapNotNull(EquipmentSlot::fromRaw)
      .toSet()
    if (declared.isNotEmpty()) return declared
    return setOfNotNull(EquipmentSlot.fromRaw(requestedSlot))
  }

  fun bonuses(item: ItemStack): EquipmentBonuses = EquipmentBonuses(
    hp = intMeta(item, "bonus.HP", "bonus.hp"),
    str = intMeta(item, "bonus.STR", "bonus.str"),
    def = intMeta(item, "bonus.DEF", "bonus.def"),
    skl = intMeta(item, "bonus.SKL", "bonus.skl"),
    vit = intMeta(item, "bonus.VIT", "bonus.vit")
  )

  fun bonuses(state: GameState, characterId: String): EquipmentBonuses {
    val character = state.characters[characterId] ?: return EquipmentBonuses()
    val inventory = state.inventories[character.inventoryId] ?: state.inventories[characterId]
    val ids = state.equipment[character.equipmentId]?.slots.orEmpty().values.distinct()
    return ids.mapNotNull { inventory?.items?.get(it) }.fold(EquipmentBonuses()) { total, item ->
      total + bonuses(item)
    }
  }

  fun weaponDamage(state: GameState, characterId: String, fallback: Int = 30): Int {
    val character = state.characters[characterId] ?: return fallback
    val weaponId = state.equipment[character.equipmentId]?.slots?.get(EquipmentSlot.WEAPON.key) ?: return fallback
    val inventory = state.inventories[character.inventoryId] ?: state.inventories[characterId]
    val item = inventory?.items?.get(weaponId) ?: return fallback
    return intMeta(item, "weaponDamage", "damage", "DMG").takeIf { it > 0 } ?: fallback
  }

  fun isEquipped(state: GameState, characterId: String, itemId: String): Boolean =
    state.equipment[characterId]?.slots.orEmpty().values.any { it == itemId }

  fun preserveMissingHp(before: GameState, after: GameState, characterId: String): GameState {
    val oldStats = CharacterStatRules.project(before, characterId) ?: return after
    val newStats = CharacterStatRules.project(after, characterId) ?: return after
    val missing = oldStats.maxHp - oldStats.currentHp
    val nextHp = if (oldStats.currentHp <= 0) 0 else (newStats.maxHp - missing).coerceAtLeast(0)
    return CharacterProgressionRules.setCurrentHp(after, characterId, nextHp)
  }

  private fun intMeta(item: ItemStack, vararg keys: String): Int {
    keys.forEach { key -> item.metadata[key]?.toIntOrNull()?.let { return it } }
    return 0
  }
}
