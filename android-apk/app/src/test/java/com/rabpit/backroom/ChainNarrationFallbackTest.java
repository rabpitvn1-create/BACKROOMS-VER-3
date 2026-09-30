package com.rabpit.backroom;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class ChainNarrationFallbackTest {
  private static JSONObject state(String result, int lastRollTurn) throws Exception {
    return new JSONObject().put("turn", 2).put("location", "Level 0 / điểm xuất phát")
        .put("levelRoute", new JSONObject().put("lastRollTurn", lastRollTurn)
            .put("lastResult", result));
  }

  @Test public void committedOutcomesProduceDistinctPlayerFacingNarration() throws Exception {
    String success = MainActivity.narrationFallback(state("SUCCESS", 2), "").getString("reply");
    String reset = MainActivity.narrationFallback(state("RESET", 2), "").getString("reply");
    String exit = MainActivity.narrationFallback(state("EXIT_AVAILABLE", 2), "").getString("reply");
    assertTrue(success.contains("tiến sâu hơn"));
    assertTrue(success.contains("lối thoát"));
    assertTrue(reset.contains("cảnh vật quen thuộc"));
    assertTrue(reset.contains("điểm xuất phát"));
    assertTrue(exit.contains("lối ra"));
    assertTrue(exit.contains("chặng tiếp theo"));
    for (String reply : new String[] {success, reset, exit}) {
      assertFalse(reply.contains("roll"));
      assertFalse(reply.contains("streak"));
      assertFalse(reply.contains("xác suất"));
    }
  }

  @Test public void committedEmergentEventSurvivesProviderFailureAlongsideActionHint()
      throws Exception {
    JSONObject state = state("SUCCESS", 2);
    state.put("emergent", new JSONObject().put("lastSelection",
        new JSONObject().put("selectedNone", false)
            .put("publicSummary", "Dấu hiệu dị thường đang tạo thêm nguy hiểm.")));
    String reply = MainActivity.narrationFallback(state, "Thông tin hành động cũ").getString("reply");
    assertTrue(reply.contains("tiến sâu hơn"));
    assertTrue(reply.contains("Dấu hiệu dị thường đang tạo thêm nguy hiểm."));
  }

  @Test public void providerTimeoutDoesNotKeepGameplayLocked() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      long started = System.nanoTime();
      try {
        MainActivity.awaitNarration(executor, () -> {
          Thread.sleep(5_000L);
          return new JSONObject();
        }, 100L);
        throw new AssertionError("Blocked provider must not delay the Core fallback");
      } catch (TimeoutException expected) {
        assertTrue("Timed-out provider must return promptly",
            java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 2_000L);
      }
    } finally {
      executor.shutdownNow();
    }
  }

  @Test public void oldOutcomeDoesNotRepeatOnNextTurn() throws Exception {
    String reply = MainActivity.narrationFallback(state("SUCCESS", 1), "").getString("reply");
    assertFalse(reply.contains("tiến sâu hơn"));
    assertTrue(reply.contains("tường vàng"));
    assertTrue(reply.contains("huỳnh quang"));
  }
  @Test public void postCombatFallbackUsesGroundedSceneInsteadOfStatusText() throws Exception {
    JSONObject levelZero = state("SUCCESS", 1).put("currentLevelKey", "0")
        .put("location", "Hành lang vàng nhạt — khu vực chưa xác định");
    String reply = MainActivity.narrationFallback(levelZero, "",
        "Quan sát khu vực sau trận chiến").getString("reply");
    assertTrue(reply.contains("Sau trận chiến"));
    assertTrue(reply.contains("tường vàng"));
    assertFalse(reply.contains("khu vực chưa xác định"));
    assertFalse(reply.contains("Inventory"));
    assertFalse(reply.contains("trạng thái"));
    String following = MainActivity.narrationFallback(levelZero, "",
        "Lắng nghe âm thanh trong khu vực hiện tại").getString("reply");
    assertTrue(following.contains("Tiếng đèn huỳnh quang"));
    assertFalse(following.equals(reply));
  }

  @Test public void chestHintPreservesItemButDoesNotReadLikeSystemReward() throws Exception {
    String reply = MainActivity.narrationFallback(state("", -1),
        "Rương chứa Almond Water x1. Đã thêm vào Inventory. Nhận +10 Core.").getString("reply");
    assertTrue(reply.contains("Cao Minh mở nắp rương"));
    assertTrue(reply.contains("Almond Water x1"));
    assertFalse(reply.contains("Inventory"));
    assertFalse(reply.contains("Nhận +10 Core"));
  }

  @Test public void fallbackOutsideLevelZeroDoesNotInventYellowWalls() throws Exception {
    JSONObject other = new JSONObject().put("turn", 4)
        .put("currentLevelKey", "2").put("location", "Phòng chưa xác định");
    String reply = MainActivity.narrationFallback(other, "", "Quan sát").getString("reply");
    assertTrue(reply.contains("Phòng chưa xác định"));
    assertFalse(reply.contains("tường vàng"));
    assertFalse(reply.contains("huỳnh quang"));
  }

  @Test public void defeatPerceptionShroudDoesNotExposeActualPositionAfterProviderFailure()
      throws Exception {
    JSONObject hidden = state("RESET", 2)
        .put("currentLevelKey", "hua_1900_0")
        .put("location", "Hui's Family Level 1 / hành lang sâu")
        .put("perceptionShroud", true);
    JSONObject generated = MainActivity.narrationFallback(hidden, "", "Quan sát lối đi");
    String reply = generated.getString("reply");
    assertTrue(reply.contains("Cao Minh"));
    assertFalse(reply.contains("Hui's Family"));
    assertFalse(reply.contains("hành lang sâu"));
    assertFalse(reply.contains("điểm xuất phát"));
    assertFalse(reply.contains("cảnh vật quen thuộc"));
  }

  @Test public void selectedEmergentEventKeepsNarrativeLeadIn() throws Exception {
    JSONObject levelZero = state("SUCCESS", 1).put("currentLevelKey", "0");
    levelZero.put("emergent", new JSONObject().put("lastSelection",
        new JSONObject().put("selectedNone", false)
            .put("publicSummary", "Dấu hiệu dị thường đang tạo thêm nguy hiểm.")));
    String reply = MainActivity.narrationFallback(levelZero, "",
        "Quan sát").getString("reply");
    assertTrue(reply.startsWith("Cao Minh nhìn dọc"));
    assertTrue(reply.contains("Dấu hiệu dị thường đang tạo thêm nguy hiểm."));
  }

}
