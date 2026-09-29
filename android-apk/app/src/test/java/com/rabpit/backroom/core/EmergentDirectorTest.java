package com.rabpit.backroom.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/** Focused regression coverage plus a provider-free 200-turn Director stress simulation. */
public class EmergentDirectorTest {
  private final EmergentTurnEngine engine = new EmergentTurnEngine();

  private static JSONObject state(int turn) throws Exception {
    return new JSONObject().put("turn", turn).put("currentLevel", 0)
        .put("currentLevelKey", "0").put("flags", new JSONObject())
        .put("party", new JSONArray()).put("log", new JSONArray());
  }

  private static JSONObject family(JSONArray candidates, String family) {
    for (int i = 0; i < candidates.length(); i++) {
      JSONObject item = candidates.optJSONObject(i);
      if (item != null && family.equals(item.optString("family"))) return item;
    }
    return null;
  }

  private void commit(JSONObject state, String id, JSONArray events, JSONObject selected) throws Exception {
    engine.commitAuthoritative(state, id, events, selected);
    engine.catchUpProjections(state);
  }

  @Test public void anomalySetupEscalatesIntoPayoffDebtAndPlayerCanInvestigateAtARealCost()
      throws Exception {
    JSONObject state = state(2);
    engine.normalizeState(state);
    JSONObject setup = family(engine.directorCandidates(state, "đi tiếp"), "ANOMALY_SETUP");
    assertTrue(setup != null);
    JSONArray first = new JSONArray();
    engine.applyDirectorSelection(state, first, "setup", setup);
    commit(state, "setup", first, setup);
    assertTrue("anomaly setup should create environmental pressure",
        CampaignSkeleton.axisScore(state.getJSONObject("emergent"), "environmental_exposure") > 0.0d);

    state.put("turn", 6);
    JSONObject payoff = family(engine.directorCandidates(state, "đi tiếp"), "ANOMALY_PAYOFF");
    assertTrue(payoff != null);
    assertTrue(payoff.getInt("payoffDebt") >= 3);

    JSONArray investigate = new JSONArray();
    engine.applyPlayerEventResponse(state, investigate, "investigate", "Điều tra dấu hiệu dị thường");
    assertEquals(1, investigate.length());
    assertEquals("EMERGENT_ANOMALY_INVESTIGATED",
        investigate.getJSONObject(0).getString("eventType"));
    assertEquals(1, state.getJSONObject("flags").getInt("anomalyInsight"));
    assertTrue(engine.ambientAlertUntilTurn(state) > state.getInt("turn"));
    assertTrue(family(engine.directorCandidates(state, "Điều tra dấu hiệu dị thường"),
        "ANOMALY_PAYOFF") == null);
    commit(state, "investigate", investigate, null);
    assertTrue(family(engine.directorCandidates(state, "đi tiếp"), "ANOMALY_PAYOFF") == null);
  }

  @Test public void payoffEscalatesHazardAndResolvesExactlyOneThread() throws Exception {
    JSONObject state = state(2);
    engine.normalizeState(state);
    JSONObject setup = family(engine.directorCandidates(state, "đi"), "ANOMALY_SETUP");
    JSONArray events = new JSONArray();
    engine.applyDirectorSelection(state, events, "setup", setup);
    commit(state, "setup", events, setup);

    state.put("turn", 9);
    JSONObject payoff = family(engine.directorCandidates(state, "đi"), "ANOMALY_PAYOFF");
    events = new JSONArray();
    engine.applyDirectorSelection(state, events, "payoff", payoff);
    assertEquals("EMERGENT_ANOMALY_PAYOFF", events.getJSONObject(0).getString("eventType"));
    assertTrue(engine.ambientAlertUntilTurn(state) >= 10);
    commit(state, "payoff", events, payoff);
    assertTrue(family(engine.directorCandidates(state, "đi"), "ANOMALY_PAYOFF") == null);
    JSONArray threads = state.getJSONObject("emergent").getJSONArray("threads");
    assertEquals("RESOLVED", threads.getJSONObject(0).getString("status"));
  }

  @Test public void chestEchoRequiresActualCommittedSourceAndCannotRepeat() throws Exception {
    JSONObject state = state(1);
    engine.normalizeState(state);
    assertTrue(family(engine.directorCandidates(state, "đi"), "RESOURCE_ECHO") == null);
    JSONArray chest = new JSONArray();
    chest.put(engine.event("chest", chest, "CHEST_OPENED", "LOCAL", "0",
        new JSONObject().put("factPredicate", "chest_opened").put("factValue", "water")
            .put("observedByPlayer", true).put("causedBy", "player"), null));
    commit(state, "chest", chest, null);

    state.put("turn", 4);
    JSONObject echo = family(engine.directorCandidates(state, "đi"), "RESOURCE_ECHO");
    assertTrue(echo != null);
    JSONArray events = new JSONArray();
    engine.applyDirectorSelection(state, events, "echo", echo);
    assertEquals("EMERGENT_RESOURCE_ECHO", events.getJSONObject(0).getString("eventType"));
    commit(state, "echo", events, echo);
    assertTrue(family(engine.directorCandidates(state, "đi"), "RESOURCE_ECHO") == null);
    assertTrue(engine.ambientAlertUntilTurn(state) > state.getInt("turn"));
  }

  @Test public void partyBeatTracksRequestUntilPlayerActuallyTalksToCompanion()
      throws Exception {
    JSONObject state = state(4).put("party", new JSONArray().put(new JSONObject().put("id", "lucia")));
    engine.normalizeState(state);
    JSONObject beat = family(engine.directorCandidates(state, "đi"), "RELATIONSHIP_BEAT");
    assertTrue(beat != null);
    JSONArray events = new JSONArray();
    engine.applyDirectorSelection(state, events, "party-beat", beat);
    commit(state, "party-beat", events, beat);
    assertTrue(family(engine.directorCandidates(state, "đi"), "RELATIONSHIP_BEAT") == null);

    state.put("turn", 6);
    JSONArray talk = new JSONArray();
    engine.applyPlayerEventResponse(state, talk, "talk", "Trò chuyện với Lucia");
    assertEquals(1, talk.length());
    commit(state, "talk", talk, null);
    JSONArray threads = state.getJSONObject("emergent").getJSONArray("threads");
    assertEquals("RESOLVED", threads.getJSONObject(0).getString("status"));
  }

  @Test public void quietEventReducesCommittedDangerPressure() throws Exception {
    JSONObject state = state(2);
    engine.normalizeState(state);
    JSONArray fights = new JSONArray();
    for (int i = 0; i < 2; i++) {
      fights.put(engine.event("combat", fights, "COMBAT_HAND_RESOLVED", "LOCAL", "hound",
          new JSONObject().put("observedByPlayer", true), null));
    }
    commit(state, "combat", fights, null);
    double before = CampaignSkeleton.axisScore(state.getJSONObject("emergent"), "entity_attention");
    assertTrue(before > 0.0d);
    state.put("turn", 3);
    JSONObject quiet = family(engine.directorCandidates(state, "nghỉ"), "QUIET");
    assertTrue(quiet != null);
    JSONArray relief = new JSONArray();
    engine.applyDirectorSelection(state, relief, "quiet", quiet);
    commit(state, "quiet", relief, quiet);
    double after = CampaignSkeleton.axisScore(state.getJSONObject("emergent"), "entity_attention");
    assertTrue("quiet beat should actually release pressure", after < before);
  }

  @Test public void emptyOrBlockedCandidatesNeverCommitUninventedEffects() throws Exception {
    JSONObject state = state(2);
    engine.normalizeState(state);
    state.getJSONObject("flags").put("entityEncounterKey", "hound");
    assertEquals(0, engine.directorCandidates(state, "đi").length());
    state.getJSONObject("flags").remove("entityEncounterKey");
    state.getJSONObject("flags").put("chestPresent", true);
    assertEquals(0, engine.directorCandidates(state, "đi").length());
    state.getJSONObject("flags").put("chestPresent", false);
    assertEquals(0, engine.directorCandidates(state, "điều tra khác").length()
        - engine.directorCandidates(state, "đi").length());
  }

  @Test public void deterministicDirectorRunsTwoIdentical200TurnSimulationsWithoutProvider()
      throws Exception {
    JSONObject initial = state(0).put("party",
        new JSONArray().put(new JSONObject().put("id", "lucia")));
    engine.normalizeState(initial); // Generate one saveId, then clone for exact replay.
    String first = simulate200(new JSONObject(initial.toString()), true);
    String replay = simulate200(new JSONObject(initial.toString()), false);
    assertEquals(first, replay);
  }

  private String simulate200(JSONObject state, boolean logDistribution) throws Exception {
    Map<String, Integer> families = new LinkedHashMap<>();
    Map<String, Integer> lastTurn = new LinkedHashMap<>();
    int quiet = 0, pressure = 0, debt = 0, combat = 0, relationship = 0, consequence = 0;
    for (int turn = 1; turn <= 200; turn++) {
      state.put("turn", turn);
      String action = turn % 11 == 0 ? "Điều tra dấu hiệu dị thường"
          : turn % 13 == 0 ? "Trò chuyện với Lucia" : "Cao Minh tiếp tục khám phá";
      String turnId = "director-simulation-" + turn;
      JSONArray events = new JSONArray();

      // Deterministic synthetic existing-world events stress the Director in isolation.
      if (turn == 1) {
        events.put(engine.event(turnId, events, "CHEST_OPENED", "LOCAL", "0",
            new JSONObject().put("factPredicate", "chest_opened").put("factValue", "water")
                .put("causedBy", "player").put("observedByPlayer", true), null));
      }
      if (turn % 40 == 0) {
        for (int i = 0; i < 2; i++) {
          events.put(engine.event(turnId, events, "COMBAT_HAND_RESOLVED", "LOCAL", "hound",
              new JSONObject().put("observedByPlayer", true), null));
          combat++;
        }
      }
      engine.applyPlayerEventResponse(state, events, turnId, action);
      JSONArray pool = engine.directorCandidates(state, action);
      int version = engine.stateVersion(state);
      TurnRng rng = new TurnRng(turnId, version,
          EmergentTurnEngine.CANON_VERSION, EmergentTurnEngine.RNG_SCHEMA_VERSION);
      JSONObject selected = engine.selectCandidate(state, pool, rng, turn);
      String family = selected.optString("family", "NONE");
      families.put(family, families.containsKey(family) ? families.get(family) + 1 : 1);
      if (!"NONE".equals(family)) {
        if (lastTurn.containsKey(family)) {
          int gap = turn - lastTurn.get(family);
          if ("ANOMALY_SETUP".equals(family)) assertTrue("setup cooldown", gap >= 10);
          if ("QUIET".equals(family)) assertTrue("quiet cooldown", gap > 4);
          if ("RELATIONSHIP_BEAT".equals(family)) assertTrue("party cooldown", gap > 14);
        }
        lastTurn.put(family, turn);
        engine.applyDirectorSelection(state, events, turnId, selected);
        if ("QUIET".equals(family)) quiet++;
        if ("RELATIONSHIP_BEAT".equals(family)) relationship++;
        if ("ANOMALY_PAYOFF".equals(family) || "RESOURCE_ECHO".equals(family)) consequence++;
      }
      commit(state, turnId, events, selected);
      JSONObject root = state.getJSONObject("emergent");
      JSONObject director = root.getJSONObject("director");
      JSONObject metrics = director.optJSONObject("pressures");
      if (metrics != null) pressure += (int) Math.round(metrics.optDouble("environmental", 0) * 100);
      JSONArray threads = root.getJSONArray("threads");
      for (int i = 0; i < threads.length(); i++) {
        JSONObject thread = threads.getJSONObject(i);
        if ("ACTIVE".equals(thread.optString("status"))) {
          debt += Math.max(0, turn - thread.optInt("lastTouchedTurn", turn));
        }
      }
      assertTrue(engine.selectionProjectionFresh(state));
    }
    assertEquals(200, state.getJSONObject("emergent").getInt("commitSequence"));
    assertTrue("At least one emergent event expected", families.size() > 1);
    assertTrue("emergent events should contribute to pressure", pressure > 0);
    String log = "families=" + families + ", quiet=" + quiet + ", combatFixtures=" + combat
        + ", relationship=" + relationship + ", consequence=" + consequence
        + ", cumulativePressure=" + pressure + ", unresolvedDebt=" + debt;
    if (logDistribution) System.out.println("200-turn Director simulation: " + log);
    return log + "; finalState=" + state.toString();
  }
}
