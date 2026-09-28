package com.rabpit.backroom.core

import com.rabpit.backroom.core.gameplay.CharacterProgressionState
import com.rabpit.backroom.core.gameplay.CoreResourceState
import com.rabpit.backroom.core.gameplay.LevelRuntimeState

const val CURRENT_SAVE_VERSION = 4
const val KAI_ID = "kai"
enum class CharacterPresence { ACTIVE, SEPARATED, MISSING, DEAD }
enum class CommandSource { RULE, GEMINI, UI, SYSTEM }
enum class PendingTurnStatus { CREATED, INTERPRETING, VALIDATING, EXECUTING, COMMITTED, FAILED }

data class ItemStack(
  val itemId: String,
  val name: String,
  val quantity: Int = 1,
  val condition: String? = null,
  val metadata: Map<String, String> = emptyMap(),
  val archetypeId: String = itemId,
  val contentState: ContentState = ContentState.NONE
)

data class InventoryState(val ownerId: String, val items: Map<String, ItemStack> = emptyMap())
data class EquipmentState(val ownerId: String, val slots: Map<String, String> = emptyMap())

data class StatusEffect(
  val id: String,
  val type: String,
  val source: String,
  val startTurnId: String? = null,
  val durationTurns: Int? = null,
  val persistent: Boolean = false,
  val metadata: Map<String, String> = emptyMap()
)

data class PhysiologyState(
  val minutesSinceFood: Long? = null,
  val minutesSinceWater: Long? = null,
  val minutesAwake: Long? = null,
  val painState: String? = null,
  val infectionState: String? = null,
  val thermalState: String? = null,
  val metadata: Map<String, String> = emptyMap()
) {
  companion object {
    /** Simulation baseline for a fresh run: needs begin satisfied at Backrooms entry. */
    fun freshRunBaseline(): PhysiologyState = PhysiologyState(
      minutesSinceFood = 0L,
      minutesSinceWater = 0L,
      minutesAwake = 0L,
      metadata = mapOf("baseline" to "fresh_run_entry")
    )
  }
}

data class CharacterState(
  val id: String,
  val name: String,
  val avatarRef: String? = null,
  val healthState: String? = null,
  val injuries: List<String> = emptyList(),
  val presence: CharacterPresence = CharacterPresence.ACTIVE,
  val inventoryId: String = id,
  val equipmentId: String = id,
  val statusIds: Set<String> = emptySet(),
  val physiology: PhysiologyState = PhysiologyState(),
  val metadata: Map<String, String> = emptyMap(),
  val progression: CharacterProgressionState = CharacterProgressionState()
)

data class PartyState(val leaderId: String = KAI_ID, val memberIds: List<String> = listOf(KAI_ID), val maxMembers: Int = 4)

data class PendingTurn(
  val turnId: String,
  val input: String,
  val status: PendingTurnStatus = PendingTurnStatus.CREATED,
  val commandIds: List<String> = emptyList(),
  val error: String? = null
)

data class TurnState(
  val currentTurnId: String = "TURN_1",
  val pending: PendingTurn? = null,
  val completedTurnIds: Set<String> = emptySet(),
  val executedCommandIds: Set<String> = emptySet()
)

data class GameTimeState(
  val elapsedSubjectiveMinutes: Long = 0L,
  val lastAdvanceMinutes: Int = 0,
  val lastAdvanceReason: String? = null
)

data class GameState(
  val characters: Map<String, CharacterState>,
  val party: PartyState = PartyState(),
  val inventories: Map<String, InventoryState> = emptyMap(),
  val equipment: Map<String, EquipmentState> = emptyMap(),
  val statuses: Map<String, StatusEffect> = emptyMap(),
  val turn: TurnState = TurnState(),
  val time: GameTimeState = GameTimeState(),
  val world: Map<String, String> = emptyMap(),
  val saveVersion: Int = CURRENT_SAVE_VERSION,
  val metadata: Map<String, String> = emptyMap(),
  val coreResource: CoreResourceState = CoreResourceState(),
  val levelRuntime: LevelRuntimeState = LevelRuntimeState()
) {
  companion object {
    fun initial(): GameState = GameState(
      characters = mapOf(
        KAI_ID to CharacterState(
          KAI_ID,
          "Player",
          physiology = PhysiologyState.freshRunBaseline()
        )
      ),
      inventories = mapOf(KAI_ID to InventoryState(KAI_ID)),
      equipment = mapOf(KAI_ID to EquipmentState(KAI_ID))
    )
  }
}
