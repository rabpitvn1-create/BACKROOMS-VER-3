package com.rabpit.backroom.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class LevelCoreChainExplorerTest {
  private final LevelCore core = new LevelCore(null, bound -> 99);

  private static JSONObject start() throws Exception {
    return new JSONObject().put("turn", 1).put("currentLevelKey", "0")
        .put("currentLevel", 0).put("location", LevelCore.LEVEL_ZERO_START_LOCATION);
  }

  // Same boundary as prepareExplorerTurnData: the action resolves in the committed turn.
  private void resolve(JSONObject state, int roll) throws Exception {
    state.put("turn", state.getInt("turn") + 1);
    core.rollRouteForExplorerAction(state, "Tiếp tục khám phá", bound -> roll);
  }

  @Test public void successIsVisibleOnlyForItsCommittedTurnAndIsIdempotent() throws Exception {
    JSONObject state = start();
    resolve(state, 20);
    JSONObject route = state.getJSONObject("levelRoute");
    assertEquals(1, route.getInt("streak"));
    assertEquals(2, route.getInt("lastRollTurn"));
    assertFalse(route.getBoolean("exitAvailable"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: SUCCESS"));
    core.rollRouteForExplorerAction(state, "Tiếp tục khám phá", bound -> {
      throw new AssertionError("Repeated prepared turn must not draw RNG");
    });
    assertEquals(1, route.getInt("streak"));

    state.put("turn", 3);
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: NO ROUTE ROLL"));
    assertFalse(core.promptContext(state).contains("OUTCOME THIS TURN: SUCCESS"));
  }

  @Test public void failureResetsStreakAndCommittedOrigin() throws Exception {
    JSONObject state = start();
    resolve(state, 20);
    state.put("location", "Level 0 / hành lang xa lạ");
    resolve(state, 80);
    JSONObject route = state.getJSONObject("levelRoute");
    assertEquals(0, route.getInt("streak"));
    assertEquals(LevelCore.LEVEL_ZERO_START_LOCATION, state.getString("location"));
    assertEquals(LevelCore.LEVEL_ZERO_START_LOCATION, route.getString("returnLocation"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: RESET"));
  }

  @Test public void onePercentRollAddsThree() throws Exception {
    JSONObject state = start();
    resolve(state, 0);
    assertEquals(3, state.getJSONObject("levelRoute").getInt("streak"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: SUCCESS"));
  }

  @Test public void exitOpensAtRequiredStreakAndTransitionsOnlyWhenRequested() throws Exception {
    JSONObject state = start();
    for (int i = 0; i < LevelCore.ROUTE_REQUIRED_STREAK; i++) resolve(state, 20);
    JSONObject route = state.getJSONObject("levelRoute");
    assertEquals(LevelCore.ROUTE_REQUIRED_STREAK, route.getInt("streak"));
    assertTrue(route.getBoolean("exitAvailable"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: EXIT_AVAILABLE"));
    assertFalse(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra đến Level 1"));
    assertEquals("0", state.getString("currentLevelKey"));

    state.put("turn", state.getInt("turn") + 1);
    assertTrue(core.promptContext(state).contains("was already identified on an earlier turn"));
    assertFalse(core.promptContext(state).contains("OUTCOME THIS TURN: EXIT_AVAILABLE"));
    assertTrue(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra"));
    assertEquals("0.1", state.getString("currentLevelKey"));
    assertEquals(LevelCore.defaultLocation("0.1"), state.getString("location"));
    assertEquals(0, state.getJSONObject("levelRoute").getInt("streak"));
    assertFalse(state.getJSONObject("levelRoute").getBoolean("exitAvailable"));
    assertEquals(-1, state.getJSONObject("levelRoute").getInt("lastRollTurn"));
    assertFalse(core.promptContext(state).contains("OUTCOME THIS TURN: EXIT_AVAILABLE"));
  }

  @Test public void transitionCannotBypassLockedExit() throws Exception {
    JSONObject state = start();
    core.normalizeState(state);
    assertFalse(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra"));
    assertEquals("0", state.getString("currentLevelKey"));
    assertEquals(LevelCore.LEVEL_ZERO_START_LOCATION, state.getString("location"));
  }

  @Test public void independentPreviewBranchesDoNotShareRouteOutcomes() throws Exception {
    JSONObject base = start();
    core.normalizeState(base);
    JSONObject success = new JSONObject(base.toString());
    JSONObject reset = new JSONObject(base.toString());
    JSONObject observation = new JSONObject(base.toString());
    success.getJSONObject("levelRoute").put("streak", 1)
        .put("originLocation", LevelCore.LEVEL_ZERO_START_LOCATION);
    reset.getJSONObject("levelRoute").put("streak", 1)
        .put("originLocation", LevelCore.LEVEL_ZERO_START_LOCATION);
    resolve(success, 20);
    resolve(reset, 80);
    observation.put("turn", 2);
    core.rollRouteForExplorerAction(observation, "Quan sát kỹ", bound -> {
      throw new AssertionError("Non-exploration branch must not roll");
    });
    assertTrue(core.promptContext(success).contains("OUTCOME THIS TURN: SUCCESS"));
    assertTrue(core.promptContext(reset).contains("OUTCOME THIS TURN: RESET"));
    assertTrue(core.promptContext(observation).contains("OUTCOME THIS TURN: NO ROUTE ROLL"));
    assertEquals(-1, base.getJSONObject("levelRoute").getInt("lastRollTurn"));
    assertFalse(core.promptContext(success).contains("OUTCOME THIS TURN: RESET"));
    assertFalse(core.promptContext(reset).contains("OUTCOME THIS TURN: SUCCESS"));
  }

  @Test public void promptDoesNotAskAiToTransitionOrSetScene() throws Exception {
    JSONObject state = start();
    core.normalizeState(state);
    assertFalse(core.promptContext(state).contains("transitionTarget"));
    assertFalse(core.promptContext(state).contains("sceneLabel"));
    assertFalse(GmNarrativePacket.build(core.promptContext(state), "", "", "", "", state,
        "Quan sát", "").contains("transitionTarget"));
  }
}
