package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class CommandResolverTest {
  private val resolver = CommandResolver()
  private val context = GameContext(
    GameState.initial().copy(
      characters = GameState.initial().characters + ("companion" to CharacterState("companion", "Companion"))
    ),
    actorAliases = mapOf("player" to KAI_ID, "companion" to "companion"),
    itemAliases = mapOf("chai nước" to "almond-water")
  )

  @Test fun resolvesActorItemQuantityAndTargetDeterministically() {
    val candidate = IntentCandidate(
      "player đưa companion hai chai nước",
      GameIntent.TRANSFER_ITEM,
      IntentConfidence.HIGH,
      .99f,
      CommandSource.RULE
    )
    val command = resolver.resolve(candidate, 0, "TURN_184", context) as ItemCommand
    assertEquals(KAI_ID, command.actorId)
    assertEquals("companion", command.targetId)
    assertEquals("almond-water", command.itemId)
    assertEquals(2, command.quantity)
    assertEquals(command.commandId, (resolver.resolve(candidate, 0, "TURN_184", context) as ItemCommand).commandId)
  }
}
