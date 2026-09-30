package com.rabpit.backroom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class HiddenNarrativeChainTest {
  private static JSONArray choices(String a, String b) throws Exception {
    return new JSONArray().put(new JSONObject().put("text", a))
        .put(new JSONObject().put("text", b));
  }

  private static JSONObject forecastStep(String before, String after) throws Exception {
    return new JSONObject().put("canonicalAction", "Tiếp tục khám phá")
        .put("beforeWorldHash", before).put("afterWorldHash", after)
        .put("state", new JSONObject().put("turn", 3));
  }

  private static HiddenNarrativeChain oneBeat(JSONArray first) throws Exception {
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("before", "after")));
    JSONObject generated = new JSONObject().put("steps", new JSONArray().put(
        new JSONObject()
            .put("replyA", "Cao Minh lần theo những chi tiết trước mắt.")
            .put("replyB", "Cao Minh đổi cách tiếp cận nhưng vẫn tiến qua khu vực.")
            .put("nextChoices", choices("Quan sát vệt nước", "Lắng nghe phía sau bức tường"))));
    return HiddenNarrativeChain.parse(forecast, generated, first);
  }

  @Test public void preparedButtonsShareOneCoreOutcomeButKeepDifferentNarration() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi", "Lần theo tiếng gió");
    HiddenNarrativeChain chain = oneBeat(first);
    HiddenNarrativeChain.Beat beat = chain.current("before", first);
    assertNotNull(beat);
    assertEquals("Tiếp tục khám phá",
        HiddenNarrativeChain.resolveCoreAction("Kiểm tra lối đi", "A", beat));
    assertEquals("Tiếp tục khám phá",
        HiddenNarrativeChain.resolveCoreAction("Lần theo tiếng gió", "B", beat));
    assertFalse(beat.narration("A").getString("reply")
        .equals(beat.narration("B").getString("reply")));
    assertFalse(chain.consume(beat));
    assertEquals(0, chain.remaining());
  }

  @Test public void multiBeatChainAdvancesAcrossMatchingWorldStatesAndChoices() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi", "Lần theo tiếng gió");
    JSONArray second = choices("Quan sát khe cửa", "Lắng nghe phía sau tường");
    JSONArray third = choices("Tiếp tục quan sát", "Đổi hướng khám phá");

    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("before", "middle"))
        .put(forecastStep("middle", "after")));
    JSONObject generated = new JSONObject().put("steps", new JSONArray()
        .put(new JSONObject()
            .put("replyA", "Cao Minh kiểm tra những dấu vết gần lối đi.")
            .put("replyB", "Cao Minh lần theo tiếng động nhỏ trong hành lang.")
            .put("nextChoices", second))
        .put(new JSONObject()
            .put("replyA", "Cao Minh quan sát kỹ khu vực vừa tới.")
            .put("replyB", "Cao Minh đổi góc tiếp cận nhưng vẫn giữ cảnh giác.")
            .put("nextChoices", third)));

    HiddenNarrativeChain chain = HiddenNarrativeChain.parse(forecast, generated, first);
    HiddenNarrativeChain.Beat firstBeat = chain.current("before", first);
    assertNotNull(firstBeat);
    assertEquals("Tiếp tục khám phá",
        HiddenNarrativeChain.resolveCoreAction("Lần theo tiếng gió", "B", firstBeat));
    assertTrue(chain.consume(firstBeat));

    HiddenNarrativeChain.Beat secondBeat = chain.current("middle", firstBeat.nextChoices);
    assertNotNull(secondBeat);
    assertEquals("Tiếp tục khám phá",
        HiddenNarrativeChain.resolveCoreAction("Quan sát khe cửa", "A", secondBeat));
    assertFalse(chain.consume(secondBeat));
    assertEquals(0, chain.remaining());
  }

  @Test public void freeformActionIsNeverReplacedByHiddenCanonicalAction() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi", "Lần theo tiếng gió");
    HiddenNarrativeChain.Beat beat = oneBeat(first).current("before", first);
    assertEquals("Tôi quay đầu bỏ chạy",
        HiddenNarrativeChain.resolveCoreAction("Tôi quay đầu bỏ chạy", null, beat));
    assertEquals("Tôi ngồi yên và không đi tiếp",
        HiddenNarrativeChain.resolveCoreAction("Tôi ngồi yên và không đi tiếp", null, beat));
  }

  @Test public void missingOrStaleChainFallsBackToSubmittedAction() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi", "Lần theo tiếng gió");
    HiddenNarrativeChain chain = oneBeat(first);
    assertNull(chain.current("stale", first));
    assertNull(chain.current("before", choices("Mở cửa", "Lần theo tiếng gió")));
    assertEquals("Lần theo tiếng gió",
        HiddenNarrativeChain.resolveCoreAction("Lần theo tiếng gió", "B", null));
  }

  @Test public void malformedChainIsRejected() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi", "Lần theo tiếng gió");
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("before", "after")));
    JSONObject malformed = new JSONObject().put("steps", new JSONArray().put(
        new JSONObject().put("replyA", "Cao Minh quan sát.")
            .put("replyB", "Cao Minh lắng nghe.")
            .put("nextChoices", new JSONArray()
                .put(new JSONObject().put("text", "Chỉ một lựa chọn")))));
    boolean rejected = false;
    try { HiddenNarrativeChain.parse(forecast, malformed, first); }
    catch (IllegalArgumentException expected) { rejected = true; }
    assertTrue(rejected);
  }
}
