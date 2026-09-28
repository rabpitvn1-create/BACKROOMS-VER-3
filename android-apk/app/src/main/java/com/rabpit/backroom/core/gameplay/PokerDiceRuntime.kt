package com.rabpit.backroom.core.gameplay

data class PokerDiceState(
  val values: List<Int> = List(PokerDiceRules.DICE_COUNT) { 0 },
  val held: List<Boolean> = List(PokerDiceRules.DICE_COUNT) { false },
  val hasRolled: Boolean = false,
  val rerollsUsed: Int = 0,
  val maxRerolls: Int = PokerDiceRules.MAX_REROLLS,
  val finalized: Boolean = false,
  val resolved: Boolean = false,
  val hand: String = ""
) {
  init {
    require(values.size == PokerDiceRules.DICE_COUNT)
    require(held.size == PokerDiceRules.DICE_COUNT)
    require(values.all { it in 0..6 })
    require(rerollsUsed in 0..maxRerolls)
    if (hasRolled) require(values.all { it in 1..6 })
  }
}

data class PokerDiceRoll(
  val state: PokerDiceState,
  val nextSequence: Int
)

object PokerDiceRuntime {
  fun newState(): PokerDiceState = PokerDiceState()

  fun ensureInitialRoll(seed: Int, sequence: Int, state: PokerDiceState): PokerDiceRoll {
    var current = state
    var nextSequence = sequence
    if (!current.hasRolled) {
      val rolled = rollDice(seed, nextSequence, current, respectHeld = false)
      current = rolled.state.copy(hasRolled = true, rerollsUsed = 0)
      nextSequence = rolled.nextSequence
      current = updateHand(autoHoldDuplicateGroups(current))
    } else if (current.hand.isBlank()) {
      current = updateHand(current)
    }
    return PokerDiceRoll(current, nextSequence)
  }

  fun setHold(state: PokerDiceState, dieIndex: Int, held: Boolean): PokerDiceState {
    require(dieIndex in 0 until PokerDiceRules.DICE_COUNT) { "Die index không hợp lệ." }
    check(state.hasRolled) { "Phải ROLL trước khi HOLD." }
    check(!state.finalized) { "Hand đã được chốt." }
    return state.copy(held = state.held.toMutableList().also { it[dieIndex] = held })
  }

  fun roll(seed: Int, sequence: Int, state: PokerDiceState): PokerDiceRoll {
    if (state.finalized) return PokerDiceRoll(state, sequence)
    if (!state.hasRolled) return ensureInitialRoll(seed, sequence, state)
    if (state.rerollsUsed >= state.maxRerolls || state.held.all { it }) {
      return PokerDiceRoll(state, sequence)
    }

    val rolled = rollDice(seed, sequence, state, respectHeld = true)
    val next = updateHand(autoHoldDuplicateGroups(
      rolled.state.copy(rerollsUsed = state.rerollsUsed + 1)
    ))
    return PokerDiceRoll(next, rolled.nextSequence)
  }

  fun finish(state: PokerDiceState): PokerDiceState {
    check(state.hasRolled) { "Dice chưa có Initial Roll." }
    return if (state.finalized) state else updateHand(state).copy(finalized = true)
  }

  fun markResolved(state: PokerDiceState): PokerDiceState =
    if (state.resolved) state else state.copy(resolved = true)

  private fun rollDice(
    seed: Int,
    sequence: Int,
    state: PokerDiceState,
    respectHeld: Boolean
  ): PokerDiceRoll {
    val values = state.values.toMutableList()
    var nextSequence = sequence
    values.indices.forEach { slot ->
      if (respectHeld && state.held[slot]) return@forEach
      values[slot] = PokerDiceRules.deterministicDie(seed, nextSequence, slot)
      nextSequence++
    }
    return PokerDiceRoll(state.copy(values = values), nextSequence)
  }

  private fun updateHand(state: PokerDiceState): PokerDiceState =
    state.copy(hand = PokerDiceRules.classify(*state.values.toIntArray()).v2Name)

  private fun autoHoldDuplicateGroups(state: PokerDiceState): PokerDiceState {
    val counts = IntArray(7)
    state.values.forEach { if (it in 1..6) counts[it]++ }
    val held = state.held.toMutableList()
    state.values.forEachIndexed { index, value ->
      if (value in 1..6 && counts[value] >= 2) held[index] = true
    }
    return state.copy(held = held)
  }
}
