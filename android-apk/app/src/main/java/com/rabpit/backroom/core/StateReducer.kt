package com.rabpit.backroom.core

object CommandValidator {
  fun validate(state: GameState, command: GameCommand): ValidationResult {
    if (command.commandId.isBlank()) return ValidationResult(false, "command_id_required")
    if (command.actorId !in state.characters) return ValidationResult(false, "actor_unknown")
    if (command.turnId != null && command.turnId != state.turn.currentTurnId) return ValidationResult(false, "turn_id_mismatch")
    if (command is ValidatedStateCommand && !command.validatedByGameEngine) return ValidationResult(false, "engine_validation_required")

    // V2 authority: loot ownership can only be created by deterministic Core/System code.
    if (command is ItemCommand && command.operation == ItemCommand.Operation.PICKUP &&
      command.source != CommandSource.SYSTEM) {
      return ValidationResult(false, "player_pickup_unavailable")
    }
    return ValidationResult(true)
  }
}

object StateReducer {
  fun execute(state: GameState, command: GameCommand): ExecutionResult {
    if (command.commandId in state.turn.executedCommandIds) {
      return ExecutionResult(state, applied = false, duplicate = true)
    }
    val validation = CommandValidator.validate(state, command)
    if (!validation.valid) return ExecutionResult(state, false, validation = validation)
    val result = when (command) {
      is ItemCommand -> InventoryEngine.execute(state, command)
      is PartyCommand -> PartyEngine.execute(state, command)
      is StatusCommand -> StatusEngine.execute(state, command)
      is StatUpgradeCommand -> ProgressionEngine.execute(state, command)
      is TimeAdvanceCommand -> TimeEngine.execute(state, command)
      is PhysiologyCommand -> PhysiologyEngine.execute(state, command)
      is QueryCommand -> ExecutionResult(state, applied = false)
      is ValidatedStateCommand -> {
        val worldPatch = mapOfNotNull(
          "location" to command.location,
          "title" to command.title,
          "levelJson" to command.levelJson,
          "flagsJson" to command.flagsJson
        )
        val metadataPatch = mapOfNotNull("playerJson" to command.playerJson)
        changed(state.copy(world = state.world + worldPatch, metadata = state.metadata + metadataPatch), "validated_world_state")
      }
    }
    if (!result.applied) return result
    val rememberedItemId = (command as? ItemCommand)?.let { rememberedItemAfter(state, result.state, it) }
    val nextMetadata = if (rememberedItemId != null) result.state.metadata + ("lastReferencedItemId" to rememberedItemId) else result.state.metadata
    return result.copy(state = result.state.copy(
      metadata = nextMetadata,
      turn = result.state.turn.copy(executedCommandIds = result.state.turn.executedCommandIds + command.commandId)
    ))
  }

  private fun rememberedItemAfter(before: GameState, after: GameState, command: ItemCommand): String =
    command.itemId

  fun executeAll(state: GameState, commands: List<GameCommand>): ExecutionResult {
    var current = state
    val events = mutableListOf<String>()
    for (command in commands) {
      val result = execute(current, command)
      if (!result.applied && !result.duplicate) return ExecutionResult(state, false, validation = result.validation)
      current = result.state
      events += result.events
    }
    return ExecutionResult(current, applied = current != state, events = events)
  }
}

private fun mapOfNotNull(vararg pairs: Pair<String, String?>): Map<String, String> = pairs.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
