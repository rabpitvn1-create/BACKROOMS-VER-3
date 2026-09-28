package com.rabpit.backroom.core.gameplay

data class PokerDiceState(
  val values: List<Int>,
  val held: List<Boolean>,
  val rerollsUsed: Int = 0,
  val sequence: Int = 0,
  val finalized: Boolean = false,
  val resolved: Boolean = false,
  val hand: PokerDiceRules.Hand = PokerDiceRules.Hand.NO_HAND
) {
  init {
    require(values.size == PokerDiceRules.DICE_COUNT)
    require(held.size == PokerDiceRules.DICE_COUNT)
    require(values.all { it in 1..6 })
    require(rerollsUsed in 0..PokerDiceRules.MAX_REROLLS)
  }
}

object PokerDiceRuntime {
  fun initial(seed: Int): PokerDiceState =
    rollUnheld(
      seed,
      PokerDiceState(
        values = List(PokerDiceRules.DICE_COUNT) { 1 },
        held = List(PokerDiceRules.DICE_COUNT) { false }
      ),
      respectHeld = false
    ).let(::autoHoldDuplicates)

  fun setHold(state: PokerDiceState, dieIndex: Int, held: Boolean): PokerDiceState {
    require(dieIndex in 0 until PokerDiceRules.DICE_COUNT) { "Die index out of range" }
    check(!state.finalized) { "Hand already finalized" }
    return state.copy(held = state.held.toMutableList().also { it[dieIndex] = held })
  }

  fun reroll(seed: Int, state: PokerDiceState): PokerDiceState {
    if (state.finalized || state.rerollsUsed >= PokerDiceRules.MAX_REROLLS || state.held.all { it }) return state
    return autoHoldDuplicates(
      rollUnheld(seed, state.copy(rerollsUsed = state.rerollsUsed + 1), respectHeld = true)
    )
  }

  fun finish(state: PokerDiceState): PokerDiceState =
    if (state.finalized) state else state.copy(finalized = true, hand = PokerDiceRules.classify(*state.values.toIntArray()))

  fun markResolved(state: PokerDiceState): PokerDiceState =
    if (state.resolved) state else state.copy(resolved = true)

  private fun rollUnheld(seed: Int, state: PokerDiceState, respectHeld: Boolean): PokerDiceState {
    val values = state.values.toMutableList()
    var sequence = state.sequence
    values.indices.forEach { slot ->
      if (respectHeld && state.held[slot]) return@forEach
      values[slot] = PokerDiceRules.deterministicDie(seed, sequence, slot)
      sequence++
    }
    return state.copy(
      values = values,
      sequence = sequence,
      hand = PokerDiceRules.classify(*values.toIntArray())
    )
  }

  private fun autoHoldDuplicates(state: PokerDiceState): PokerDiceState {
    val counts = state.values.groupingBy { it }.eachCount()
    val held = state.held.toMutableList()
    state.values.forEachIndexed { index, value -> if ((counts[value] ?: 0) >= 2) held[index] = true }
    return state.copy(held = held)
  }
}
