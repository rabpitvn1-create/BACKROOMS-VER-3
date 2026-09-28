package com.rabpit.backroom.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
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
    assertCandidateRate(one, "character:luc_tram", 10.0d);

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
    String canonText = new String(Files.readAllBytes(canonPath), StandardCharsets.UTF_8);
    Map<String, String> canonFiles = new LinkedHashMap<>();
    canonFiles.put("Lucia_Codex.md", canonText);
    CanonRetriever.CanonPacket packet =
        new CanonRetriever(canonFiles).retrieve(levelZero, "Lucia Lục", CanonRetriever.DEFAULT_BUDGET, true);
    assertTrue(packet.promptText().contains(
        "Lucia Lục / Hứa Thuý Mai và Lục Trầm là hai nhân vật khác nhau"));
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
