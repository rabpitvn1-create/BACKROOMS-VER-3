package com.rabpit.backroom.core

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateCodecTest {
  @Test fun roundTripPreservesCurrentTypedState() {
    val state = GameState.initial().copy(
      inventories = mapOf(PLAYER_ID to InventoryState(PLAYER_ID, mapOf(
        "water" to ItemStack("water", "Almond Water", 2)
      ))),
      turn = TurnState("TURN_9", PendingTurn("TURN_9", "search", PendingTurnStatus.INTERPRETING)),
      time = GameTimeState(485L, 15, "travel")
    )

    assertEquals(state, GameStateCodec.decode(GameStateCodec.encode(state)))
  }

  @Test fun oldSaveVersionResetsInsteadOfMigrating() {
    val old = JSONObject(GameStateCodec.encode(GameState.initial())).put("saveVersion", CURRENT_SAVE_VERSION - 1)
    assertEquals(GameState.initial(), GameStateCodec.decode(old))
  }
}
