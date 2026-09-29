package com.rabpit.backroom.core;

import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
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

  @Test public void completedCombatCannotEraseNextCommittedEncounter() throws Exception {
    EntityCore core = new EntityCore(
        "{\"rollMode\":\"independent_per_entity\",\"entities\":["
            + "{\"key\":\"hound\",\"name\":\"Hound\",\"ratePercent\":3.5,\"canon\":\"hunts\"}]}");
    JSONObject state = new JSONObject()
        .put("turn", 8).put("currentLevel", 0).put("flags", new JSONObject())
        .put("combat", new JSONObject().put("active", false).put("outcome", "victory")
            .put("entity", new JSONObject().put("key", "hound")));
    core.activateEncounterCandidate(state, "hound");
    assertFalse("Terminal combat must be retired when a new encounter commits", state.has("combat"));
    CombatChoiceEngine.normalizeTerminalEncounter(state);
    assertEquals("hound", state.getJSONObject("flags").getString("entityEncounterKey"));
    try {
      core.activateEncounterCandidate(state, "hound");
      org.junit.Assert.fail("A second encounter must be rejected while the first is active");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("already active"));
    }
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
