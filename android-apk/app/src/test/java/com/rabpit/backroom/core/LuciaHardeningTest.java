package com.rabpit.backroom.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LuciaHardeningTest {
  @Test public void luciaRemainsDistinctAndLevelZeroOnly() throws Exception {
    CharacterEncounterCore encounters = new CharacterEncounterCore();

    JSONObject levelZero = state(0, "0");
    JSONArray zero = encounters.situationCandidates(levelZero);
    assertCandidateRate(zero, "character:lucia", 10.0d);
    assertFalse(zero.toString().contains("character:luc_tram"));

    JSONObject sublevel = state(0, "0.1");
    assertFalse(encounters.situationCandidates(sublevel).toString().contains("character:lucia"));

    JSONObject levelOne = state(1, "1");
    JSONArray one = encounters.situationCandidates(levelOne);
    assertFalse(one.toString().contains("character:lucia"));
    assertCandidateRate(one, "character:luc_tram", 0.25d);

    encounters.activateEncounterCandidate(levelZero, "lucia");
    JSONObject lucia = levelZero.getJSONArray("party").getJSONObject(0);
    assertEquals("lucia", lucia.getString("id"));
    assertEquals("Lucia Lục", lucia.getString("name"));
    assertTrue(lucia.getJSONArray("inventory").toString().contains("M4A1 cá nhân hóa"));

    try {
      encounters.activateEncounterCandidate(sublevel, "lucia");
      fail("Lucia must not materialize outside original Level 0.");
    } catch (IllegalStateException expected) {
      assertEquals(0, sublevel.getJSONArray("party").length());
    }

    JSONObject distinct = state(1, "1").put("party", new JSONArray()
        .put(new JSONObject().put("id", "lucia").put("name", "Hứa Thuý Mai"))
        .put(new JSONObject().put("id", "luc_tram").put("name", "Lục Trầm")));
    encounters.normalizeState(distinct);
    assertEquals(2, distinct.getJSONArray("party").length());
    assertEquals("lucia", distinct.getJSONArray("party").getJSONObject(0).getString("id"));
    assertEquals("luc_tram", distinct.getJSONArray("party").getJSONObject(1).getString("id"));

    CharacterProgressionCore progression = new CharacterProgressionCore();
    progression.normalizeState(distinct);
    assertEquals("lucia", CharacterProgressionCore.normalizeCharacterId("Hứa Thuý Mai"));
    assertEquals("luc_tram", CharacterProgressionCore.normalizeCharacterId("Lục Trầm"));
    assertTrue(distinct.getJSONObject(CharacterProgressionCore.ROOT_KEY)
        .getJSONObject(CharacterProgressionCore.CHARACTERS_KEY).has("lucia"));

    assertTrue(CombatChoiceEngine.hasAuthoritativeUltimate("lucia"));
    assertTrue(CombatChoiceEngine.hasAuthoritativeUltimate("luc_tram"));
    assertEquals(5, CombatChoiceEngine.characterProcCount("lucia"));
    assertEquals(5, CombatChoiceEngine.characterProcCount("luc_tram"));

    Path canonPath = Paths.get("src/main/assets/canon/Lucia_Codex.md");
    if (!Files.isRegularFile(canonPath)) canonPath = Paths.get("app/src/main/assets/canon/Lucia_Codex.md");
    if (!Files.isRegularFile(canonPath)) {
      canonPath = Paths.get("android-apk/app/src/main/assets/canon/Lucia_Codex.md");
    }
    String canonText = new String(Files.readAllBytes(canonPath), StandardCharsets.UTF_8);
    Map<String, String> canonFiles = new LinkedHashMap<>();
    canonFiles.put("Lucia_Codex.md", canonText);
    CanonRetriever.CanonPacket packet =
        new CanonRetriever(canonFiles).retrieve(levelZero, "Lucia Lục", CanonRetriever.DEFAULT_BUDGET, true);
    assertTrue(packet.promptText().contains(
        "Lucia Lục / Hứa Thuý Mai và Lục Trầm là hai nhân vật khác nhau"));
  }

  @Test public void luciaHasTwoDiceSkillsAndDiceDrivenUltimate() throws Exception {
    Field skillsField = CombatChoiceEngine.class.getDeclaredField("SKILLS");
    skillsField.setAccessible(true);
    Map<?, ?> pools = (Map<?, ?>) skillsField.get(null);
    List<?> luciaSkills = (List<?>) pools.get("lucia");
    assertEquals(2, luciaSkills.size());

    Field nameField = luciaSkills.get(0).getClass().getDeclaredField("name");
    nameField.setAccessible(true);
    assertEquals("M4A1 Joint Attack", nameField.get(luciaSkills.get(0)));
    assertEquals("M4A1 Tactical Burst", nameField.get(luciaSkills.get(1)));
    assertEquals(5, CombatChoiceEngine.characterProcCount("lucia"));

    JSONObject combatState = state(0, "0");
    new CharacterEncounterCore().activateEncounterCandidate(combatState, "lucia");
    CombatChoiceEngine.start(combatState, "diep_minh", 0);

    JSONObject combat = combatState.getJSONObject("combat");
    JSONObject entity = combat.getJSONObject("entity");
    entity.put("hp", 100000).put("maxHp", 100000);

    finalizeAs(combatState, 2, 2, 1, 4, 6);
    CombatChoiceEngine.resolveFinalized(combatState);
    assertEquals("Lucia Lục", combat.getString("currentActor"));

    JSONObject selectedSkill = combat.getJSONObject("currentSkill");
    String selectedName = selectedSkill.getString("name");
    assertTrue("M4A1 Joint Attack".equals(selectedName)
        || "M4A1 Tactical Burst".equals(selectedName));

    JSONObject currentUltimate = combat.getJSONObject("currentUltimate");
    assertEquals("Too Young To Die", currentUltimate.getString("name"));
    assertEquals(60, currentUltimate.getInt("hitCount"));
    assertEquals(15, currentUltimate.getInt("bonusPercent"));

    entity.put("hp", 100000).put("maxHp", 100000)
        .put("bleedTurns", 0).put("bleedPercent", 0)
        .put("poisonTurns", 0).put("poisonPercent", 0)
        .put("armorBreakTurns", 0).put("armorBreakPercent", 0)
        .put("accuracyPenaltyTurns", 0).put("accuracyPenalty", 0)
        .put("stunTurns", 0);
    JSONObject lucia = combat.getJSONArray("participants").getJSONObject(1);
    int currentDamage = CombatChoiceEngine.basicDamage(
        lucia.getInt("baseAttack"), lucia.getInt("STR"), 100);
    int expected = CombatChoiceEngine.ultimateDamage(currentDamage, 60, 15, 100);

    finalizeAs(combatState, 1, 2, 3, 4, 5);
    CombatChoiceEngine.resolveFinalized(combatState);

    assertEquals(100000 - expected, entity.getInt("hp"));
    assertTrue(combatState.getJSONArray("log").getJSONObject(0)
        .getJSONArray("battleLog").toString().contains("Too Young To Die"));
  }

  private static void finalizeAs(JSONObject state, int... values) throws Exception {
    JSONObject dice = state.getJSONObject("combat").getJSONObject("diceState");
    JSONArray array = new JSONArray();
    for (int value : values) array.put(value);
    dice.put("values", array)
        .put("hasRolled", true)
        .put("finalized", true)
        .put("resolved", false)
        .put("hand", CombatChoiceEngine.classify(values));
  }

  private static JSONObject state(int level, String levelKey) throws Exception {
    return new JSONObject()
        .put("currentLevel", level)
        .put(LevelCore.LEVEL_KEY, levelKey)
        .put("turn", 1)
        .put("player", new JSONObject().put("name", "Cao Minh"))
        .put("party", new JSONArray())
        .put("flags", new JSONObject())
        .put("log", new JSONArray().put(new JSONObject().put("role", "gm").put("text", "Test")));
  }

  private static void assertCandidateRate(JSONArray candidates, String key, double expected) throws Exception {
    for (int i = 0; i < candidates.length(); i++) {
      JSONObject candidate = candidates.getJSONObject(i);
      if (key.equals(candidate.optString("situationKey"))) {
        assertEquals(expected, candidate.getDouble("chancePercent"), 0.0000001d);
        return;
      }
    }
    fail("Missing candidate " + key);
  }
}
