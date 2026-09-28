package com.rabpit.backroom.core

import org.junit.Assert.*
import org.junit.Test

class RuleIntentInterpreterTest {
  private val parser = RuleIntentInterpreter()
  private val context = GameContext(GameState.initial())

  private fun parse(text: String) = parser.interpretSync(text, context)

  @Test fun deterministicCommandsStayLocal() {
    assertEquals(GameIntent.PICKUP_ITEM, parse("nhặt chai nước").candidates.single().intent)
    assertEquals(GameIntent.PARTY_JOIN_REQUEST, parse("Companion vào party").candidates.single().intent)
    assertFalse(parse("nhặt chai nước").requiresFallback)
  }

  @Test fun narrativeMemoryNegationAndQuotesDoNotExecute() {
    listOf(
      "nhìn người khác lấy chai nước",
      "nhớ lần trước mình bỏ súng xuống",
      "không nhặt chai nước",
      "người kia nói: “nhặt chai nước lên”"
    ).forEach { assertEquals(GameIntent.NO_ACTION, parse(it).candidates.single().intent) }
  }

  @Test fun unknownRequiresFallback() {
    val result = parse("cân nhắc tình hình kỳ lạ trước mặt")
    assertEquals(GameIntent.UNKNOWN, result.candidates.single().intent)
    assertTrue(result.requiresFallback)
  }
}
