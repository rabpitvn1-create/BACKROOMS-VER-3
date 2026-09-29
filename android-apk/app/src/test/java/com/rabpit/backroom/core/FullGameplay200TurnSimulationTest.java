package com.rabpit.backroom.core;

import static org.junit.Assert.*;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Full 200-player-action integration harness: actual facade, shipped graph/registry,
 * route, inventory/chest, NPC encounter, Director, persistence, narration contract,
 * and real Poker Dice combat start/finish/resolution. No provider or synthetic fight event.
 *
 * Two explicit profiles keep "natural new game" separate from "existing Lucia companion".
 */
public class FullGameplay200TurnSimulationTest {
  private static final int WORLD_ACTIONS = 200;
  private static final int COMBAT_HAND_LIMIT = 160;

  @Test public void realGameFlowFor200ActionsWithCombatEncountersAndNarration() throws Exception {
    Result natural = play("full-natural-20260929", false, WORLD_ACTIONS);
    Result repeatPrefix = play("full-natural-20260929", false, 30);
    assertEquals("Fixed seed must replay identical real gameplay through 30 actions",
        natural.prefix(30), repeatPrefix.trace.toString());
    assertEquals(WORLD_ACTIONS, natural.actions);
    assertTrue("Simulation must exercise actual entity encounter selection", natural.entityEncounters > 0);
    assertTrue("Simulation must resolve real Poker Dice hands", natural.combatHands > 0);
    assertEquals("No encounter may be silently abandoned",
        natural.entityEncounters, natural.combatWins + natural.combatLosses);
    System.out.println("FULL_GAMEPLAY_200_NATURAL " + natural.report());

    Result party = play("full-party-20260929", true, WORLD_ACTIONS);
    assertEquals(WORLD_ACTIONS, party.actions);
    assertTrue("Established-party profile must generate actual relationship beats",
        party.family.getOrDefault("RELATIONSHIP_BEAT", 0) > 0);
    assertTrue("Established-party profile must resolve real Poker Dice hands", party.combatHands > 0);
    System.out.println("FULL_GAMEPLAY_200_ESTABLISHED_PARTY " + party.report());
  }

  private static Result play(String seed, boolean establishedParty, int targetActions) throws Exception {
    String graph = asset("level_graph.json");
    String knowledge = asset("knowledge/level_knowledge.json");
    String registry = asset("knowledge/entity_encounters.json");
    // The source files above are the actual checked-in assets, not simplified test copies.
    JSONObject initial = GameCoreFacade.newGameState(new JSONObject())
        .put("emergent", new JSONObject().put("saveId", seed));
    if (establishedParty) {
      // A scenario with an already recruited character, created with the real encounter rule.
      new CharacterEncounterCore().activateEncounterCandidate(initial, "lucia");
    }

    SharedPreferences prefs = inMemoryPreferences(initial.toString());
    Result result = new Result(seed, establishedParty);
    try (GameCoreFacade core = new GameCoreFacade(
        prefs, LevelCore.withAssets(knowledge, graph), new EntityCore(registry), false)) {
      JSONObject state = new JSONObject(core.normalizeState(initial.toString()));
      if (establishedParty) {
        state = narrateAndCommit(core, state, "(prior first contact)", result);
      }
      for (int step = 1; step <= targetActions; step++) {
        JSONObject before = new JSONObject(core.currentCoreState());
        String action = chooseAction(before, step);
        JSONObject prepared = new JSONObject(core.processRule(before.toString(), action));
        assertEquals("Failed to prepare explorer action " + step + ": " + prepared, "turn_prepared",
            prepared.optString("reason"));
        assertFalse(prepared.optBoolean("handled", true));
        String turnId = prepared.getString("turnId");
        JSONObject selected = prepared.getJSONObject("selectedCandidate");
        JSONObject committed = new JSONObject(core.completePreparedTurn(turnId, "{}"));
        assertTrue("Core did not commit explorer action " + step + ": " + committed,
            committed.optBoolean("handled", false));
        state = committed.getJSONObject("state");
        assertEquals("At least one event must commit with each world action",
            "turn_committed", committed.getString("reason"));
        assertEquals(turnId, state.getJSONObject("emergent").getString("lastCommittedTurnId"));
        result.countCommit(state);
        String family = selected.optString("family",
            selected.optBoolean("selectedNone", false) ? "NONE" : selected.optString("kind", "UNKNOWN"));
        result.countFamily(family, step);
        result.countLocation(state.optString("currentLevelKey", "?"));

        // Real committed post-turn state goes through the real prompt builder and guard.
        String hash = core.currentStateHash();
        String prompt = GmNarrativePacket.build("", "", "", "", "", state,
            action, "", "");
        assertTrue(prompt.contains("CAMPAIGN CONTINUITY"));
        assertEquals("Prompt construction must never mutate the live Core", hash,
            core.currentStateHash());
        state = narrateAndCommit(core, state, action, result);

        String entity = state.optJSONObject("flags") == null ? ""
            : state.getJSONObject("flags").optString("entityEncounterKey", "");
        if (!entity.isEmpty()) {
          assertTrue("Only real registered combat entities can start fights",
              CombatChoiceEngine.isKnownEntity(entity));
          result.entityEncounters++;
          result.enemy.merge(entity, 1, Integer::sum);
          int gmIndex = state.optJSONArray("log") == null ? 0 : state.getJSONArray("log").length() - 1;
          state = new JSONObject(core.startCombatRuntime(entity, Math.max(0, gmIndex)));
          assertTrue("Encounter must lead to a real active Poker Dice combat",
              CombatChoiceEngine.isActive(state));
          boolean finished = false;
          for (int hand = 0; hand < COMBAT_HAND_LIMIT; hand++) {
            state = new JSONObject(core.combatFinishRuntime());
            JSONObject resolved = new JSONObject(core.processCombatResolution(state.toString()));
            assertTrue("Real combat resolution rejected: " + resolved,
                resolved.optBoolean("handled", false));
            state = resolved.getJSONObject("state");
            result.countCommit(state);
            result.combatHands++;
            if (!CombatChoiceEngine.isActive(state)) {
              String outcome = state.optJSONObject("combat") == null ? ""
                  : state.getJSONObject("combat").optString("outcome");
              if ("victory".equals(outcome)) result.combatWins++;
              else if ("defeat".equals(outcome)) result.combatLosses++;
              else fail("Combat finished without an authoritative terminal outcome: " + outcome);
              finished = true;
              break;
            }
          }
          assertTrue("Combat was not resolvable within the generous 160-hand bound", finished);
        }
        JSONObject after = new JSONObject(core.currentCoreState());
        result.trace.add(step + ":" + family + ":" + after.optString("currentLevelKey") + ":"
            + after.getJSONObject("emergent").optInt("commitSequence")
            + ":" + result.combatHands + ":" + result.combatWins + ":" + result.combatLosses);
        result.actions++;
        assertEquals("Authoritative state must remain complete",
            after.getJSONObject("emergent").getInt("commitSequence"),
            after.getJSONObject("emergent").getJSONObject("projectionWatermarks")
                .getInt("selectionCooldown"));
      }
      JSONObject end = new JSONObject(core.currentCoreState());
      JSONObject root = end.getJSONObject("emergent");
      JSONArray threads = root.getJSONArray("threads");
      result.finalTurn = end.getInt("turn");
      result.commits = root.getInt("commitSequence");
      result.unresolved = 0;
      result.debt = 0;
      for (int i = 0; i < threads.length(); i++) {
        JSONObject thread = threads.optJSONObject(i);
        if (thread == null) continue;
        if ("ACTIVE".equals(thread.optString("status"))
            || "DORMANT".equals(thread.optString("status"))) {
          result.unresolved++;
          result.debt += Math.max(0,
              result.finalTurn - thread.optInt("lastTouchedTurn", result.finalTurn));
        }
      }
      result.partyMembers = end.optJSONArray("party") == null ? 0 : end.getJSONArray("party").length();
      JSONObject pressures = root.getJSONObject("director").getJSONObject("pressures");
      result.environmentPressure = pressures.optDouble("environmental", 0.0d);
      result.resourcePressure = pressures.optDouble("resource", 0.0d);
      result.dangerPressure = pressures.optDouble("danger", 0.0d);
      return result;
    }
  }

  private static JSONObject narrateAndCommit(GameCoreFacade core, JSONObject state,
                                              String action, Result result) throws Exception {
    JSONObject encounter = state.optJSONObject("characterEncounter");
    JSONArray pending = encounter == null ? null : encounter.optJSONArray("pendingIntro");
    JSONArray dialogue = pending != null && pending.length() > 0
        ? new JSONArray().put("Cao Minh nhận ra người vừa xuất hiện.")
            .put("Người đồng hành nói rằng sẽ cùng anh tìm đường.")
        : new JSONArray();
    JSONObject proposal = new JSONObject()
        .put("reply", "Cao Minh tiếp tục quan sát sự thay đổi của hành lang.")
        .put("choices", new JSONArray())
        .put("encounterDialogue", dialogue);
    assertEquals("Deterministic provider-free narrator stub must satisfy real guard",
        "", NarrationGuard.validate(proposal, state, action));
    JSONArray log = state.optJSONArray("log");
    if (log == null) log = new JSONArray();
    log.put(new JSONObject().put("role", "player").put("text", action));
    log.put(new JSONObject().put("role", "gm").put("text", proposal.getString("reply"))
        .put("battleLog", new JSONArray()));
    state.put("log", log);
    state = new JSONObject(core.commitNarration(state.toString(), dialogue.length() > 0, true));
    if (dialogue.length() > 0) result.characterIntros += pending.length();
    return state;
  }

  private static String chooseAction(JSONObject state, int step) {
    JSONObject flags = state.optJSONObject("flags");
    if (flags != null && flags.optBoolean("chestPresent", false)) return ItemCore.OPEN_CHEST_ACTION;
    JSONObject route = state.optJSONObject("levelRoute");
    if (route != null && route.optBoolean("exitAvailable", false)) return "Đi qua lối ra";
    JSONObject root = state.optJSONObject("emergent");
    JSONArray threads = root == null ? null : root.optJSONArray("threads");
    if (threads != null) for (int i = threads.length() - 1; i >= 0; i--) {
      JSONObject thread = threads.optJSONObject(i);
      if (thread == null || !"ACTIVE".equals(thread.optString("status"))) continue;
      JSONArray refs = thread.optJSONArray("keyRefs");
      String ref = refs == null ? "" : refs.optString(0, "");
      if (step % 7 == 0 && "ENVIRONMENTAL_MYSTERY".equals(thread.optString("threadType"))
          && ref.equals(state.optString("currentLevelKey"))) {
        return "Điều tra dấu hiệu dị thường";
      }
      if (step % 9 == 0 && "PARTY_RELATIONSHIP".equals(thread.optString("threadType"))) {
        return "Trò chuyện với " + ("luc_tram".equals(ref) ? "Lục Trầm" : ref);
      }
    }
    return step % 5 == 0 ? "Khảo sát và tìm lối ra" : "Tiếp tục khám phá";
  }

  private static SharedPreferences inMemoryPreferences(String saved) {
    // Facade persists state in-process; it reads SharedPreferences only for its initial checkpoint.
    return (SharedPreferences) Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),
        new Class<?>[] {SharedPreferences.class}, (proxy, method, args) -> {
          if ("getString".equals(method.getName())) {
            return "manual_save_json".equals(args[0]) ? saved : args[1];
          }
          if ("contains".equals(method.getName())) return "manual_save_json".equals(args[0]);
          throw new UnsupportedOperationException("Unexpected preference call: " + method.getName());
        });
  }

  private static String asset(String name) throws Exception {
    Path direct = Paths.get("src", "main", "assets", name);
    Path fromRoot = Paths.get("app", "src", "main", "assets", name);
    Path file = Files.isRegularFile(direct) ? direct : fromRoot;
    assertTrue("Simulation requires real shipped asset " + name + " in " + Paths.get("").toAbsolutePath(),
        Files.isRegularFile(file));
    return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
  }

  private static final class Result {
    final String seed;
    final boolean establishedParty;
    final Map<String, Integer> family = new LinkedHashMap<>();
    final Map<String, Integer> event = new LinkedHashMap<>();
    final Map<String, Integer> enemy = new LinkedHashMap<>();
    final Map<String, Integer> levels = new LinkedHashMap<>();
    final Map<String, Integer> lastFamilyStep = new LinkedHashMap<>();
    final Map<String, Integer> minSpacing = new LinkedHashMap<>();
    final List<String> trace = new ArrayList<>();
    int actions, commits, finalTurn, entityEncounters, characterIntros;
    int combatHands, combatWins, combatLosses, unresolved, debt, partyMembers;
    double dangerPressure, resourcePressure, environmentPressure;
    Result(String seed, boolean establishedParty) {
      this.seed = seed;
      this.establishedParty = establishedParty;
    }

    void countLocation(String key) {
      levels.merge(key, 1, Integer::sum);
    }

    void countFamily(String key, int step) {
      family.merge(key, 1, Integer::sum);
      Integer prior = lastFamilyStep.put(key, step);
      if (prior != null) minSpacing.merge(key, step - prior, Math::min);
    }

    void countCommit(JSONObject state) throws Exception {
      JSONObject root = state.getJSONObject("emergent");
      JSONArray commits = root.getJSONArray("commitLog");
      if (commits.length() == 0) return;
      JSONObject last = commits.getJSONObject(commits.length() - 1);
      JSONArray events = last.getJSONArray("events");
      for (int i = 0; i < events.length(); i++) {
        JSONObject e = events.optJSONObject(i);
        if (e != null) event.merge(e.optString("eventType", "?"), 1, Integer::sum);
      }
    }

    String prefix(int count) {
      return trace.subList(0, count).toString();
    }

    String report() throws Exception {
      return new JSONObject()
          .put("seed", seed).put("profile", establishedParty ? "established_party" : "natural")
          .put("explorerActions", actions).put("coreFinalTurn", finalTurn)
          .put("commits", commits).put("candidateFamilies", new JSONObject(family))
          .put("eventTypes", new JSONObject(event)).put("minSpacingWorldActions", new JSONObject(minSpacing))
          .put("enemyEncounters", new JSONObject(enemy)).put("visitedLevelActions", new JSONObject(levels))
          .put("combatStarts", entityEncounters).put("combatHands", combatHands)
          .put("combatWins", combatWins).put("combatLosses", combatLosses)
          .put("characterIntros", characterIntros).put("partyMembers", partyMembers)
          .put("quietBeats", family.getOrDefault("QUIET", 0))
          .put("consequences", family.getOrDefault("ANOMALY_PAYOFF", 0)
              + family.getOrDefault("RESOURCE_ECHO", 0))
          .put("relationshipBeats", family.getOrDefault("RELATIONSHIP_BEAT", 0))
          .put("unresolvedThreads", unresolved).put("payoffDebt", debt)
          .put("dangerPressure", dangerPressure).put("resourcePressure", resourcePressure)
          .put("environmentPressure", environmentPressure)
          .toString();
    }
  }
}
