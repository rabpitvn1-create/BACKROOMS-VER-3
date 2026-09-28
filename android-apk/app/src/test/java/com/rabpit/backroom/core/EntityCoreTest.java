package com.rabpit.backroom.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityCoreTest {
  @Test public void roamingEntitiesAreEligibleOnEveryCurrentLevel() {
    for (int level = 0; level <= 6; level++) {
      assertTrue("Level " + level + " must allow roaming Entities", EntityCore.roamingAllowedOn(level));
    }
  }

  @Test public void roamingPolicyDoesNotHardCodeCurrentLevelRange() {
    assertTrue(EntityCore.roamingAllowedOn(7));
    assertTrue(EntityCore.roamingAllowedOn(99));
    assertFalse(EntityCore.roamingAllowedOn(-1));
  }

  @Test public void autoSpawnRateBoundsReflectPlusTwoPointIncrease() {
    assertTrue(EntityCore.validAutoSpawnRatePercent(3.0d));
    assertTrue(EntityCore.validAutoSpawnRatePercent(3.5d));
    assertFalse(EntityCore.validAutoSpawnRatePercent(2.99d));
    assertFalse(EntityCore.validAutoSpawnRatePercent(3.51d));
  }

  @Test public void treasureSpawnAllowsFourPercentWithoutChangingOrdinaryBounds() {
    assertFalse(EntityCore.validAutoSpawnRatePercent(4.0d));
    assertTrue(EntityCore.validTreasureAutoSpawnRatePercent(4.0d));
    assertFalse(EntityCore.validTreasureAutoSpawnRatePercent(0.0d));
    assertFalse(EntityCore.validTreasureAutoSpawnRatePercent(4.01d));
  }

  @Test public void legacyBossPromptCarriesCanonWithoutAutoSpawnSemantics() {
    String prompt = EntityCore.legacyPromptContext(
        "jane_the_killer", "Jane", "Legacy hostile encounter.");

    assertTrue(prompt.contains("Jane"));
    assertTrue(prompt.contains("Legacy hostile encounter"));
    assertTrue(prompt.contains("legacy/boss-only"));
    assertTrue(prompt.contains("must not be treated as an auto-spawn Entity"));
  }
}
