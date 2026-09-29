package com.rabpit.backroom.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/** Compact actor-visible continuity derived from committed events, not speculative narration. */
final class NarrativeContinuityPacket {
  static final int MAX_CHARS = 950;
  private static final int COOLDOWN_TURNS = 4;

  private NarrativeContinuityPacket() {}

  static String build(JSONObject state) throws Exception {
    StringBuilder output = new StringBuilder("CAMPAIGN CONTINUITY (read-only; only Cao Minh's known history):\n");
    int count = 0;
    for (Item item : select(state)) {
      String line = "- " + item.summary + " [ref=" + item.id + "]\n";
      if (output.length() + line.length() > MAX_CHARS) break;
      output.append(line);
      count++;
    }
    if (count == 0) output.append("(không có ký ức dài hạn liên quan lượt này)");
    return output.toString();
  }

  /** Only call after the actual narrator reply was validated and committed, never from preview. */
  static void acknowledgeValidatedNarration(JSONObject state) throws Exception {
    JSONObject root = state == null ? null : state.optJSONObject(EmergentTurnEngine.ROOT_KEY);
    if (root == null) return;
    JSONObject marks = root.optJSONObject("continuitySurfaces");
    if (marks == null) marks = new JSONObject();
    int turn = Math.max(1, state.optInt("turn", 1));
    for (Item item : select(state)) {
      marks.put(item.id, new JSONObject().put("lastSurfacedTurn", turn));
    }
    root.put("continuitySurfaces", marks);
  }

  private static ArrayList<Item> select(JSONObject state) throws Exception {
    ArrayList<Item> output = new ArrayList<>();
    JSONObject root = state == null ? null : state.optJSONObject(EmergentTurnEngine.ROOT_KEY);
    if (root == null) return output;
    int turn = Math.max(1, state.optInt("turn", 1));
    String level = state.optString("currentLevelKey", String.valueOf(state.optInt("currentLevel", 0)));
    JSONObject surfaces = root.optJSONObject("continuitySurfaces");

    // Epistemic boundary: a stored world fact is eligible only if Cao Minh has learned it.
    Set<String> knownFacts = new HashSet<>();
    JSONArray beliefs = root.optJSONArray("beliefs");
    if (beliefs != null) for (int i = 0; i < beliefs.length(); i++) {
      JSONObject belief = beliefs.optJSONObject(i);
      if (belief != null && "cao_minh".equals(belief.optString("actorId"))
          && !belief.optString("confirmedFactId").isEmpty()) {
        knownFacts.add(belief.optString("confirmedFactId"));
      }
    }

    // A thread is visible only if it originated in an event observed by Cao Minh.
    Set<String> knownThreads = new HashSet<>();
    JSONArray commits = root.optJSONArray("commitLog");
    if (commits != null) for (int i = 0; i < commits.length(); i++) {
      JSONObject commit = commits.optJSONObject(i);
      JSONArray events = commit == null ? null : commit.optJSONArray("events");
      if (events == null) continue;
      for (int e = 0; e < events.length(); e++) {
        JSONObject event = events.optJSONObject(e);
        JSONObject params = event == null ? null : event.optJSONObject("params");
        if (params == null || !params.optBoolean("observedByPlayer", false)
            || !knownFacts.contains("fact:" + event.optString("eventId"))) continue;
        JSONArray effects = event.optJSONArray("threadEffects");
        if (effects == null) continue;
        for (int j = 0; j < effects.length(); j++) {
          JSONObject effect = effects.optJSONObject(j);
          if (effect == null) continue;
          String id = effect.optString("targetThreadId");
          if (id.isEmpty()) id = EmergentTurnEngine.deterministicThreadId(
              effect.optString("threadType"), effect.optJSONArray("keyRefs"));
          knownThreads.add(id);
        }
      }
    }

    JSONArray threads = root.optJSONArray("threads");
    if (threads != null) for (int i = 0; i < threads.length(); i++) {
      JSONObject thread = threads.optJSONObject(i);
      if (thread == null) continue;
      String id = thread.optString("threadId");
      String status = thread.optString("status");
      String type = thread.optString("threadType");
      if (!knownThreads.contains(id) || cooling(surfaces, id, turn)
          || !("ACTIVE".equals(status) || "DORMANT".equals(status))
          || !("ENVIRONMENTAL_MYSTERY".equals(type)
            || "PARTY_RELATIONSHIP".equals(type)
            || "LUC_TRAM_RELATIONSHIP".equals(type)
            || "SOCIAL_CONTACT".equals(type))) continue;
      JSONArray refs = thread.optJSONArray("keyRefs");
      String ref = refs == null ? "" : refs.optString(0, "");
      boolean overlap = level.equals(ref) || present(state.optJSONArray("party"), ref);
      int age = Math.max(0, turn - thread.optInt("lastTouchedTurn", turn));
      int priority = 8 + Math.min(9, age) + (overlap ? 15 : 0);
      String why = overlap ? "connected to current level/companion" : "unresolved prior contact";
      output.add(new Item(id, priority, "THREAD " + type + " (" + status
          + ") subject=" + ref + "; relevant now=" + why));
    }

    JSONArray facts = root.optJSONArray("historicalFacts");
    if (facts != null) for (int i = facts.length() - 1, scanned = 0;
        i >= 0 && scanned < 80; i--, scanned++) {
      JSONObject fact = facts.optJSONObject(i);
      if (fact == null) continue;
      String id = fact.optString("factId");
      String type = fact.optString("predicate");
      if (!knownFacts.contains(id) || cooling(surfaces, id, turn)
          || !("world_anomaly_investigated".equals(type)
            || "world_anomaly_payoff".equals(type)
            || "world_resource_echo".equals(type)
            || "party_request_answered".equals(type))) continue;
      int age = Math.max(0, turn - fact.optInt("turn", turn));
      if (age > 40) continue;
      String ref = fact.optString("subjectRef");
      JSONObject significance = fact.optJSONObject("significance");
      int importance = significance == null ? 0 : Math.min(3, Math.max(0, significance.optInt("importance", 0)));
      int priority = 7 + importance * 2 + Math.max(0, 9 - age)
          + ((level.equals(ref) || present(state.optJSONArray("party"), ref)) ? 10 : 0);
      output.add(new Item(id, priority, "FACT " + type + " subject=" + ref
          + "; relevant now=previous committed consequence"));
    }

    Collections.sort(output, Comparator.comparingInt((Item item) -> item.priority)
        .reversed().thenComparing(item -> item.id));
    JSONObject selection = root.optJSONObject("lastSelection");
    int max = selection != null && (selection.optBoolean("selectedNone", false)
        || "QUIET".equals(selection.optString("family"))) ? 1 : 2;
    if (output.size() > max) return new ArrayList<>(output.subList(0, max));
    return output;
  }

  private static boolean cooling(JSONObject surfaces, String id, int turn) {
    JSONObject mark = surfaces == null ? null : surfaces.optJSONObject(id);
    return mark != null && turn <= mark.optInt("lastSurfacedTurn", -100000) + COOLDOWN_TURNS;
  }

  private static boolean present(JSONArray party, String ref) {
    if (party == null || ref.isEmpty()) return false;
    for (int i = 0; i < party.length(); i++) {
      JSONObject member = party.optJSONObject(i);
      if (member != null && ref.equals(member.optString("id"))) return true;
    }
    return false;
  }

  private static final class Item {
    final String id;
    final int priority;
    final String summary;
    Item(String id, int priority, String summary) {
      this.id = id;
      this.priority = priority;
      this.summary = summary;
    }
  }
}
