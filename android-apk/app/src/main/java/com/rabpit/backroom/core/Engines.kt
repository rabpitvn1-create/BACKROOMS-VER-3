package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.CharacterProgressionRules
import com.rabpit.backroom.core.gameplay.CharacterStat
import com.rabpit.backroom.core.gameplay.CharacterStatRules
import com.rabpit.backroom.core.gameplay.EquipmentRules
import com.rabpit.backroom.core.gameplay.GameplayCatalog
import kotlin.math.max

// CharacterStatEngine.applyCompletedTurnRegen is invoked by TurnCoordinator after a completed turn.

private fun addItem(inventory: InventoryState, rawItem: ItemStack): InventoryState {
  val item = GameplayCatalog.decorate(rawItem)
  val old = inventory.items[item.itemId]
  val merged = if (old == null) item else old.copy(quantity = old.quantity + item.quantity)
  return inventory.copy(items = inventory.items + (item.itemId to merged))
}

private fun removeItem(inventory: InventoryState, itemId: String, quantity: Int): InventoryState? {
  val old = inventory.items[itemId] ?: return null
  if (quantity <= 0 || old.quantity < quantity) return null
  val items = if (old.quantity == quantity) inventory.items - itemId
  else inventory.items + (itemId to old.copy(quantity = old.quantity - quantity))
  return inventory.copy(items = items)
}

private fun useCatalogItem(
  state: GameState,
  source: InventoryState,
  command: ItemCommand,
  item: com.rabpit.backroom.core.gameplay.GameplayItem
): ExecutionResult {
  val targetId = command.targetId ?: command.actorId
  val projection = CharacterStatRules.project(state, targetId) ?: return invalid(state, "target_unknown")
  if (item.effect.hp > 0 && targetId != state.party.leaderId && projection.currentHp <= 0) {
    return invalid(state, "companion_downed")
  }
  val nextInventory = removeItem(source, command.itemId, command.quantity)
    ?: return invalid(state, "insufficient_item_quantity")
  var next = state.copy(inventories = state.inventories + (command.actorId to nextInventory))
  val character = next.characters[targetId] ?: return invalid(state, "target_unknown")
  val factor = command.quantity.coerceAtLeast(1)
  val physiology = character.physiology.copy(
    minutesSinceFood = restoreCounter(
      character.physiology.minutesSinceFood,
      PhysiologyStatusPolicy.FOOD_CRITICAL_MINUTES,
      item.effect.hunger * factor
    ),
    minutesSinceWater = restoreCounter(
      character.physiology.minutesSinceWater,
      PhysiologyStatusPolicy.WATER_CRITICAL_MINUTES,
      item.effect.thirst * factor
    )
  )
  next = next.copy(characters = next.characters + (targetId to character.copy(physiology = physiology)))
  val events = mutableListOf("item_consumed")
  if (item.effect.hunger > 0) events += "physiology_food_restored"
  if (item.effect.thirst > 0) events += "physiology_water_restored"
  if (item.effect.hp > 0) {
    val healed = CharacterProgressionRules.heal(next, targetId, item.effect.hp * factor)
    next = healed.first
    if (healed.second > 0) events += "hp_restored"
  }
  return ExecutionResult(next, applied = true, events = events)
}

private fun useItem(state: GameState, source: InventoryState, command: ItemCommand): ExecutionResult {
  val owned = source.items[command.itemId] ?: return invalid(state, "item_not_owned")
  if (owned.quantity < command.quantity) return invalid(state, "insufficient_item_quantity")
  val item = GameplayCatalog.itemFor(owned) ?: return invalid(state, "item_not_consumable")
  return useCatalogItem(state, source, command, item)
}

object InventoryEngine {
  fun execute(state: GameState, command: ItemCommand): ExecutionResult {
    if (command.quantity <= 0) return invalid(state, "quantity_must_be_positive")
    val source = state.inventories[command.actorId] ?: InventoryState(command.actorId)
    val item = GameplayCatalog.decorate(
      ItemStack(command.itemId, command.itemName, command.quantity, metadata = command.metadata)
    )
    return when (command.operation) {
      ItemCommand.Operation.PICKUP -> {
        changed(state.copy(inventories = state.inventories + (command.actorId to addItem(source, item))), "inventory_pickup")
      }
      ItemCommand.Operation.DROP -> {
        if (EquipmentRules.isEquipped(state, command.actorId, command.itemId)) return invalid(state, "item_equipped")
        val next = removeItem(source, command.itemId, command.quantity) ?: return invalid(state, "insufficient_item_quantity")
        changed(state.copy(inventories = state.inventories + (command.actorId to next)), "inventory_remove")
      }
      ItemCommand.Operation.USE -> useItem(state, source, command)
      ItemCommand.Operation.TRANSFER -> {
        val targetId = command.targetId ?: return invalid(state, "target_required")
        if (!state.characters.containsKey(targetId)) return invalid(state, "target_unknown")
        val owned = source.items[command.itemId] ?: return invalid(state, "item_not_owned")
        if (owned.quantity < command.quantity) return invalid(state, "insufficient_item_quantity")
        if (EquipmentRules.isEquipped(state, command.actorId, command.itemId)) return invalid(state, "item_equipped")
        val transferred = owned.copy(quantity = command.quantity)
        val targetInventory = state.inventories[targetId] ?: InventoryState(targetId)
        val from = removeItem(source, command.itemId, command.quantity) ?: return invalid(state, "insufficient_item_quantity")
        val to = addItem(targetInventory, transferred)
        changed(state.copy(inventories = state.inventories + (command.actorId to from) + (targetId to to)), "inventory_transfer")
      }
      ItemCommand.Operation.EQUIP -> {
        val owned = source.items[command.itemId] ?: return invalid(state, "item_not_owned")
        if (owned.quantity < 1) return invalid(state, "item_not_owned")
        val targetSlots = EquipmentRules.occupiedSlots(owned, command.slot)
        if (targetSlots.isEmpty()) return invalid(state, "equipment_slot_required")
        val equipment = state.equipment[command.actorId] ?: EquipmentState(command.actorId)
        val slots = equipment.slots.toMutableMap()
        targetSlots.forEach { slots[it.key] = command.itemId }
        val equipped = state.copy(equipment = state.equipment + (command.actorId to equipment.copy(slots = slots)))
        changed(EquipmentRules.preserveMissingHp(state, equipped, command.actorId), "item_equipped")
      }
      ItemCommand.Operation.UNEQUIP -> {
        val equipment = state.equipment[command.actorId] ?: return invalid(state, "equipment_missing")
        if (command.itemId !in equipment.slots.values) return invalid(state, "item_not_equipped")
        val unequipped = state.copy(equipment = state.equipment + (
          command.actorId to equipment.copy(slots = equipment.slots.filterValues { it != command.itemId })
        ))
        changed(EquipmentRules.preserveMissingHp(state, unequipped, command.actorId), "item_unequipped")
      }
    }
  }
}

object PartyEngine {
  fun execute(state: GameState, command: PartyCommand): ExecutionResult = when (command.operation) {
    PartyCommand.Operation.ADD -> {
      if (!state.characters.containsKey(command.targetId)) return invalid(state, "target_unknown")
      if (!command.targetPresent) return invalid(state, "target_not_present")
      if (!command.consentConfirmed) return invalid(state, "join_not_confirmed")
      if (command.targetId in state.party.memberIds) return invalid(state, "already_in_party")
      if (state.party.memberIds.size >= state.party.maxMembers) return invalid(state, "party_full")
      val character = state.characters.getValue(command.targetId)
      val physiology = character.physiology
      val joined = if (physiology.minutesSinceFood == null && physiology.minutesSinceWater == null && physiology.minutesAwake == null) {
        character.copy(physiology = PhysiologyState.freshRunBaseline())
      } else character
      changed(state.copy(
        party = state.party.copy(memberIds = state.party.memberIds + command.targetId),
        characters = state.characters + (command.targetId to joined)
      ), "party_member_added")
    }
    PartyCommand.Operation.REMOVE -> {
      if (command.targetId == state.party.leaderId) return invalid(state, "cannot_remove_leader")
      if (command.targetId !in state.party.memberIds) return invalid(state, "not_in_party")
      changed(state.copy(party = state.party.copy(memberIds = state.party.memberIds - command.targetId)), "party_member_removed")
    }
    PartyCommand.Operation.SET_LEADER -> {
      if (command.targetId !in state.party.memberIds) return invalid(state, "leader_not_in_party")
      changed(state.copy(party = state.party.copy(leaderId = command.targetId)), "party_leader_changed")
    }
    PartyCommand.Operation.SEPARATE -> {
      val character = state.characters[command.targetId] ?: return invalid(state, "target_unknown")
      changed(state.copy(characters = state.characters + (command.targetId to character.copy(presence = CharacterPresence.SEPARATED))), "party_member_separated")
    }
    PartyCommand.Operation.FOLLOW, PartyCommand.Operation.QUERY -> ExecutionResult(state, applied = false)
  }
}

object ProgressionEngine {
  fun execute(state: GameState, command: StatUpgradeCommand): ExecutionResult {
    if (CombatRuntime.active(state) != null) return invalid(state, "combat_locked")
    val character = state.characters[command.targetId] ?: return invalid(state, "target_unknown")
    val stat = runCatching { CharacterStat.valueOf(command.stat.trim().uppercase()) }.getOrNull()
      ?: return invalid(state, "stat_invalid")
    val cost = CharacterProgressionRules.upgradeCost(character.progression.stats.value(stat))
    if (state.coreResource.quantity < cost) return invalid(state, "insufficient_core")
    return try {
      changed(CharacterProgressionRules.upgrade(state, command.targetId, stat.name).state, "character_stat_upgraded")
    } catch (_: IllegalArgumentException) {
      invalid(state, "stat_invalid")
    } catch (_: IllegalStateException) {
      invalid(state, "core_upgrade_rejected")
    }
  }
}

object StatusEngine {
  fun execute(state: GameState, command: StatusCommand): ExecutionResult {
    if (!state.characters.containsKey(command.targetId)) return invalid(state, "target_unknown")
    return when (command.operation) {
      StatusCommand.Operation.APPLY -> {
        val effect = command.effect ?: return invalid(state, "status_effect_required")
        if (effect.id in state.statuses) return invalid(state, "status_already_exists")
        val character = state.characters.getValue(command.targetId)
        changed(state.copy(statuses = state.statuses + (effect.id to effect), characters = state.characters + (command.targetId to character.copy(statusIds = character.statusIds + effect.id))), "status_applied")
      }
      StatusCommand.Operation.REMOVE -> {
        val id = command.statusId ?: return invalid(state, "status_id_required")
        if (id !in state.statuses) return invalid(state, "status_missing")
        val character = state.characters.getValue(command.targetId)
        changed(state.copy(statuses = state.statuses - id, characters = state.characters + (command.targetId to character.copy(statusIds = character.statusIds - id))), "status_removed")
      }
      StatusCommand.Operation.UPDATE -> {
        val effect = command.effect ?: return invalid(state, "status_effect_required")
        if (effect.id !in state.statuses) return invalid(state, "status_missing")
        changed(state.copy(statuses = state.statuses + (effect.id to effect)), "status_updated")
      }
      StatusCommand.Operation.QUERY -> ExecutionResult(state, applied = false)
    }
  }
}

object TimeEngine {
  fun execute(state: GameState, command: TimeAdvanceCommand): ExecutionResult {
    if (command.minutes <= 0) return invalid(state, "time_minutes_must_be_positive")
    val reason = command.reason.trim()
    if (reason.isEmpty()) return invalid(state, "time_reason_required")
    val delta = command.minutes.toLong()
    val elapsed = state.time.elapsedSubjectiveMinutes
    if (elapsed > Long.MAX_VALUE - delta) return invalid(state, "time_overflow")

    val nextCharacters = linkedMapOf<String, CharacterState>()
    state.characters.forEach { (id, character) ->
      if (character.presence == CharacterPresence.DEAD) {
        nextCharacters[id] = character
        return@forEach
      }
      val physiology = character.physiology
      val food = advanceKnownCounter(physiology.minutesSinceFood, delta) ?: if (physiology.minutesSinceFood != null) return invalid(state, "physiology_time_overflow") else null
      val water = advanceKnownCounter(physiology.minutesSinceWater, delta) ?: if (physiology.minutesSinceWater != null) return invalid(state, "physiology_time_overflow") else null
      val awake = advanceKnownCounter(physiology.minutesAwake, delta) ?: if (physiology.minutesAwake != null) return invalid(state, "physiology_time_overflow") else null
      nextCharacters[id] = character.copy(physiology = physiology.copy(
        minutesSinceFood = food,
        minutesSinceWater = water,
        minutesAwake = awake
      ))
    }

    val nextTime = state.time.copy(
      elapsedSubjectiveMinutes = elapsed + delta,
      lastAdvanceMinutes = command.minutes,
      lastAdvanceReason = reason
    )
    return changed(state.copy(time = nextTime, characters = nextCharacters), "time_advanced")
  }

  private fun advanceKnownCounter(value: Long?, delta: Long): Long? {
    if (value == null) return null
    if (value < 0L || value > Long.MAX_VALUE - delta) return null
    return value + delta
  }
}

internal fun invalid(state: GameState, reason: String) = ExecutionResult(state, false, validation = ValidationResult(false, reason))
internal fun changed(state: GameState, event: String) = ExecutionResult(state, true, events = listOf(event))
