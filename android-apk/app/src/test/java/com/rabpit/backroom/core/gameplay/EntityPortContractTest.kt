package com.rabpit.backroom.core.gameplay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityPortContractTest {
  @Test fun diepMinhUsesV2CombatProfileAndAutoSpawnsAtThreePointFivePercent() {
    val entity = GameplayCatalog.entity("diep_minh")!!
    assertTrue(entity.autoSpawn)
    assertEquals(3.5, entity.autoSpawnRatePercent, 0.0)
    assertEquals(setOf(0, 1, 2, 3, 4, 5, 6), entity.levels)
    assertEquals(3, entity.skills.size)
    assertEquals(3, GameplayCatalog.entity("jane_the_killer")!!.skills.size)
    assertEquals(3, GameplayCatalog.entity("slenderman")!!.skills.size)
  }
}
