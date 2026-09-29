package com.rabpit.backroom.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/** Actor visibility, bounded packet and actual-commit-only continuity cooldown. */
public class NarrativeContinuityPacketTest {
  @Test public void onlyObservedCommittedThreadsEnterNarratorContinuity() throws Exception {
    EmergentTurnEngine engine = new EmergentTurnEngine();
    JSONObject state = new JSONObject().put("turn", 3).put("currentLevelKey", "0")
        .put("currentLevel", 0).put("party", new JSONArray()).put("flags", new JSONObject());
    engine.normalizeState(state);
    JSONArray events = new JSONArray();
    events.put(engine.event("visible", events, "EMERGENT_ANOMALY_FOUND", "REGIONAL", "0",
        new JSONObject().put("factPredicate", "world_anomaly_detected")
            .put("factValue", "anomaly:0:1").put("observedByPlayer", true),
        new JSONArray().put(engine.threadEffect("ENVIRONMENTAL_MYSTERY",
            new JSONArray().put("0").put("anomaly:0:1"), "SEED_OR_ADVANCE", null))));
    events.put(engine.event("visible", events, "EMERGENT_ANOMALY_FOUND", "REGIONAL", "hidden",
        new JSONObject().put("factPredicate", "world_anomaly_detected")
            .put("factValue", "SECRET_NOT_KNOWN_TO_PLAYER").put("observedByPlayer", false),
        new JSONArray().put(engine.threadEffect("ENVIRONMENTAL_MYSTERY",
            new JSONArray().put("hidden").put("secret"), "SEED_OR_ADVANCE", null))));
    engine.commitAuthoritative(state, "visible", events, null);
    engine.catchUpProjections(state);

    String before = state.toString();
    String packet = NarrativeContinuityPacket.build(state);
    assertTrue(packet.contains("ENVIRONMENTAL_MYSTERY"));
    assertTrue(packet.contains("subject=0"));
    assertFalse(packet.contains("SECRET_NOT_KNOWN_TO_PLAYER"));
    assertFalse(packet.contains("subject=hidden"));
    assertTrue(packet.length() <= NarrativeContinuityPacket.MAX_CHARS);
    assertEquals(before, state.toString()); // Prompt construction never mutates save state.
  }

  @Test public void surfacingCooldownAppliesOnlyOnExplicitAcknowledgement() throws Exception {
    EmergentTurnEngine engine = new EmergentTurnEngine();
    JSONObject state = new JSONObject().put("turn", 2).put("currentLevelKey", "0")
        .put("currentLevel", 0).put("party", new JSONArray()).put("flags", new JSONObject());
    engine.normalizeState(state);
    JSONArray events = new JSONArray();
    events.put(engine.event("one", events, "EMERGENT_ANOMALY_FOUND", "REGIONAL", "0",
        new JSONObject().put("observedByPlayer", true)
            .put("factPredicate", "world_anomaly_detected").put("factValue", "anomaly:0:1"),
        new JSONArray().put(engine.threadEffect("ENVIRONMENTAL_MYSTERY",
            new JSONArray().put("0").put("anomaly:0:1"), "SEED_OR_ADVANCE", null))));
    engine.commitAuthoritative(state, "one", events, null);
    engine.catchUpProjections(state);
    assertTrue(NarrativeContinuityPacket.build(state).contains("ENVIRONMENTAL_MYSTERY"));
    assertFalse(state.getJSONObject("emergent").has("continuitySurfaces")); // Preview-safe.
    NarrativeContinuityPacket.acknowledgeValidatedNarration(state);
    assertFalse(NarrativeContinuityPacket.build(state).contains("ENVIRONMENTAL_MYSTERY"));
    state.put("turn", 7);
    assertTrue(NarrativeContinuityPacket.build(state).contains("ENVIRONMENTAL_MYSTERY"));
  }
}
