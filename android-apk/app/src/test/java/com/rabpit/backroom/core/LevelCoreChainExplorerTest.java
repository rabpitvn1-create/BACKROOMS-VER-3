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
    resolve(state, 150);
    JSONObject route = state.getJSONObject("levelRoute");
    assertEquals(0, route.getInt("streak"));
    assertEquals(LevelCore.LEVEL_ZERO_START_LOCATION, state.getString("location"));
    assertEquals(LevelCore.LEVEL_ZERO_START_LOCATION, route.getString("returnLocation"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: RESET"));
  }

  @Test public void onePercentRollAddsThree() throws Exception {
    JSONObject state = start();
    resolve(state, 99);
    assertEquals(3, state.getJSONObject("levelRoute").getInt("streak"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: SUCCESS"));
  }

  @Test public void allTwoHundredRollsHaveExactBonusAndResetDistribution() throws Exception {
    int[] counts = new int[6];
    for (int roll = 0; roll < 200; roll++) {
      JSONObject state = start();
      final int value = roll;
      state.put("turn", 2);
      core.rollRouteForExplorerAction(state, "Tiếp tục khám phá", bound -> {
        assertEquals(200, bound);
        return value;
      });
      JSONObject route = state.getJSONObject("levelRoute");
      int gained = route.getInt("streak");
      counts[gained]++;
      assertEquals(gained == 0 ? "RESET" : "SUCCESS", route.getString("lastResult"));
    }
    assertEquals(97, counts[1]); // 48.5%
    assertEquals(2, counts[2]);  // 1%
    assertEquals(2, counts[3]);  // 1%
    assertEquals(1, counts[5]);  // 0.5%
    assertEquals(98, counts[0]); // 49%
    assertEquals(200, counts[0] + counts[1] + counts[2] + counts[3] + counts[5]);
  }

  @Test public void oldSixChainExitIsLockedUntilTenAndBonusCapsAtTen() throws Exception {
    JSONObject state = start();
    state.put("levelRoute", new JSONObject().put("levelKey", "0").put("streak", 6)
        .put("exitAvailable", true));
    core.normalizeState(state);
    assertEquals(6, state.getJSONObject("levelRoute").getInt("streak"));
    assertFalse(state.getJSONObject("levelRoute").getBoolean("exitAvailable"));
    assertFalse(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra"));
    resolve(state, 101);
    assertEquals(10, state.getJSONObject("levelRoute").getInt("streak"));
    assertTrue(state.getJSONObject("levelRoute").getBoolean("exitAvailable"));
    assertTrue(core.promptContext(state).contains("OUTCOME THIS TURN: EXIT_AVAILABLE"));
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
    assertEquals("hua_1900_0", state.getString("currentLevelKey"));
    assertEquals(LevelCore.defaultLocation("hua_1900_0"), state.getString("location"));
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

  @Test public void hua1900FloorsRunInOrderBetweenZeroAndOne() throws Exception {
    JSONObject state = start();
    String[] route = LevelCore.levelZeroProgressionKeys();
    assertEquals(18, route.length);
    assertEquals("0", route[0]);
    assertEquals("1", route[17]);
    for (int i = 0; i < 16; i++) assertEquals("hua_1900_" + i, route[i + 1]);
    for (int i = 1; i < route.length; i++) {
      assertFalse(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra"));
      core.normalizeState(state);
      state.getJSONObject("levelRoute").put("streak", LevelCore.ROUTE_REQUIRED_STREAK);
      assertTrue(core.applyPlayerTransitionIfRequested(state, "Đi qua lối ra"));
      assertEquals(route[i], state.getString("currentLevelKey"));
    }
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
    resolve(reset, 150);
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
