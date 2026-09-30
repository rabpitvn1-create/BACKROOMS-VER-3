package com.rabpit.backroom.core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CanonRetrieverStableIndexTest {
  @Test public void stableIdsAndOwnerKnowledgeAreSelectedWithoutSecretLeak() throws Exception {
    JSONArray items = baseItems()
        .put(item("character.luc_tram.foundation", "character:luc_tram", "SCENE",
            new JSONArray().put("character:luc_tram"), true, "Lục Trầm baseline"))
        .put(item("knowledge.luc_tram.tang_kiem_coc.observed", "character:luc_tram", "OWNER",
            new JSONArray().put("character:luc_tram"), false, "OBSERVED_ONLY"))
        .put(item("knowledge.luc_tram.tang_kiem_coc.backstage", "writer", "SECRET",
            new JSONArray().put("character:luc_tram"), false, "BACKSTAGE_SECRET"));

    CanonRetriever retriever = CanonRetriever.fromIndexJson(index(items).toString());
    JSONObject state = state().put("party", new JSONArray().put(
        new JSONObject().put("id", "luc_tram").put("joined", true)));
    String before = state.toString();

    CanonRetriever.CanonPacket packet = retriever.retrieve(state,
        "knowledge.luc_tram.tang_kiem_coc.observed", 20000, true, "Level 0");

    assertEquals(before, state.toString());
    assertTrue(packet.missingMandatoryRefs.toString(), packet.missingMandatoryRefs.isEmpty());
    assertTrue(packet.promptText().contains("character.luc_tram.foundation"));
    assertTrue(packet.promptText().contains("OBSERVED_ONLY"));
    assertFalse(packet.promptText().contains("BACKSTAGE_SECRET"));
    assertFalse(packet.promptText().contains("knowledge.luc_tram.tang_kiem_coc.backstage"));
  }

  @Test public void pendingIntroGetsCharacterCanonBeforePartyCommit() throws Exception {
    JSONArray items = baseItems().put(item("character.lucia.foundation", "character:lucia", "SCENE",
        new JSONArray().put("character:lucia"), true, "Lucia current identity"));
    CanonRetriever retriever = CanonRetriever.fromIndexJson(index(items).toString());
    JSONObject state = state().put("characterEncounter",
        new JSONObject().put("pendingIntro", new JSONArray().put("lucia")));

    CanonRetriever.CanonPacket packet = retriever.retrieve(state, "intro", 20000, false, "Level 0");

    assertTrue(packet.missingMandatoryRefs.toString(), packet.missingMandatoryRefs.isEmpty());
    assertTrue(packet.promptText().contains("character.lucia.foundation"));
  }

  @Test public void beliefGateUsesCommittedEpistemicEvidence() throws Exception {
    JSONArray items = baseItems()
        .put(item("character.luc_tram.foundation", "character:luc_tram", "SCENE",
            new JSONArray().put("character:luc_tram"), true, "Lục Trầm baseline"))
        .put(item("knowledge.luc_tram.claim.test", "character:luc_tram", "BELIEF:claim:test",
            new JSONArray().put("character:luc_tram"), false, "BELIEF_VISIBLE"));
    CanonRetriever retriever = CanonRetriever.fromIndexJson(index(items).toString());
    JSONObject state = state().put("party", new JSONArray().put(
        new JSONObject().put("id", "luc_tram").put("joined", true)));

    CanonRetriever.CanonPacket hidden = retriever.retrieve(state,
        "knowledge.luc_tram.claim.test", 20000, false, "Level 0");
    assertFalse(hidden.promptText().contains("BELIEF_VISIBLE"));

    state.put(EmergentTurnEngine.ROOT_KEY, new JSONObject().put("beliefs", new JSONArray().put(
        new JSONObject().put("actorId", "luc_tram").put("claimId", "claim:test"))));
    CanonRetriever.CanonPacket visible = retriever.retrieve(state,
        "knowledge.luc_tram.claim.test", 20000, false, "Level 0");
    assertTrue(visible.promptText().contains("BELIEF_VISIBLE"));
  }

  @Test public void missingMandatoryDependencyIsReported() throws Exception {
    JSONObject level = item("level.0.foundation", "world:level:0", "SCENE",
        new JSONArray().put("level:0"), true, "Level 0");
    level.put("requires", new JSONArray().put("missing.required.id"));
    CanonRetriever retriever = CanonRetriever.fromIndexJson(index(new JSONArray()
        .put(level)
        .put(item("character.cao_minh.foundation", "character:cao_minh", "SCENE",
            new JSONArray().put("character:cao_minh"), true, "Cao Minh"))).toString());

    CanonRetriever.CanonPacket packet = retriever.retrieve(state(), "walk", 20000, false, "Level 0");

    assertFalse(packet.missingMandatoryRefs.isEmpty());
    assertTrue(packet.missingMandatoryRefs.get(0).contains("missing.required.id"));
  }

  private static JSONArray baseItems() throws Exception {
    return new JSONArray()
        .put(item("level.0.foundation", "world:level:0", "SCENE",
            new JSONArray().put("level:0"), true, "Level 0"))
        .put(item("character.cao_minh.foundation", "character:cao_minh", "SCENE",
            new JSONArray().put("character:cao_minh"), true, "Cao Minh"));
  }

  private static JSONObject index(JSONArray items) throws Exception {
    return new JSONObject().put("schemaVersion", 1).put("items", items);
  }

  private static JSONObject item(String id, String owner, String knownBy, JSONArray scope,
      boolean core, String text) throws Exception {
    return new JSONObject()
        .put("id", id)
        .put("owner", owner)
        .put("type", "FOUNDATION")
        .put("status", "CURRENT")
        .put("knownBy", knownBy)
        .put("scope", scope)
        .put("requires", new JSONArray())
        .put("refs", new JSONArray())
        .put("core", core)
        .put("promptEligible", !"SECRET".equals(knownBy) && !"SYSTEM".equals(knownBy))
        .put("sourceFile", "canon/test.md")
        .put("text", text);
  }

  private static JSONObject state() throws Exception {
    return new JSONObject()
        .put("currentLevel", 0)
        .put(LevelCore.LEVEL_KEY, "0")
        .put("party", new JSONArray())
        .put("flags", new JSONObject())
        .put("turn", 1);
  }
}
