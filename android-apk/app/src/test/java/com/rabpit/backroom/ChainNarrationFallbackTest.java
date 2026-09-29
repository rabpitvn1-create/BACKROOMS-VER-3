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
    assertTrue(success.contains("chưa tìm thấy lối thoát"));
    assertTrue(reset.contains("trở lại khu vực quen thuộc"));
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
    assertTrue(reply.contains("tiếp tục quan sát"));
  }
}
