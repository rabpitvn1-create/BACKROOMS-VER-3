package com.rabpit.backroom.core.gameplay

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.EnumMap

/** Scoped deterministic RNG for gameplay only. No GM/canon version participates in the seed. */
class GameplayRng(
  private val turnId: String,
  private val stateVersion: Int
) {
  enum class Scope { ROUTE, SITUATION_SELECTION, LOOT, COMBAT }
  private val counters = EnumMap<Scope, Int>(Scope::class.java).apply {
    Scope.entries.forEach { put(it, 0) }
  }

  init { require(turnId.isNotBlank()) }

  fun nextInt(scope: Scope, bound: Int): Int {
    require(bound > 0)
    return (nextPositiveLong(scope) % bound.toLong()).toInt()
  }

  fun nextUnit(scope: Scope): Double =
    (nextPositiveLong(scope) ushr 10).toDouble() / 9_007_199_254_740_992.0

  fun drawsUsed(scope: Scope): Int = counters[scope] ?: 0

  private fun nextPositiveLong(scope: Scope): Long {
    val sequence = counters[scope] ?: 0
    counters[scope] = sequence + 1
    val key = "$turnId|${scope.name}|$sequence|${stateVersion.coerceAtLeast(0)}|gameplay-v3"
    val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(StandardCharsets.UTF_8))
    return ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long and Long.MAX_VALUE
  }
}
