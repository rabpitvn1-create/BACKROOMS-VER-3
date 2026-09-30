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

  private static JSONObject draftStep(String a, String b, JSONArray next) throws Exception {
    return new JSONObject().put("replyA", a).put("replyB", b)
        .put("nextChoices", next);
  }

  @Test public void bothChoicesReachSameCanonicalOutcomeWithoutConsumingTwice() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi trước mặt", "Lần theo tiếng gió từ phía sau");
    JSONArray second = choices("Quan sát những vệt ố trên tường", "Lắng nghe âm thanh từ bên kia");
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("before-1", "after-1"))
        .put(forecastStep("after-1", "after-2")));
    JSONObject generated = new JSONObject().put("steps", new JSONArray()
        .put(draftStep("Bức tường ẩm ướt kéo dài trước mắt Cao Minh.",
            "Một luồng gió lạnh đưa Cao Minh đến khoảng sáng phía trước.", second))
        .put(draftStep("Vết ố chuyển dần thành những đường nét kỳ dị.",
            "Âm thanh lạ ngừng lại khi ánh sáng phía trước hiện rõ.",
            choices("Khảo sát nền nhà gần đó", "Tiến tới vùng ánh sáng xa xa"))));

    HiddenNarrativeChain chain = HiddenNarrativeChain.parse(forecast, generated, first);
    HiddenNarrativeChain.Beat firstBeat = chain.current("before-1", first);
    assertNotNull(firstBeat);
    assertEquals(firstBeat.afterWorldHash, firstBeat.afterWorldHash);
    assertFalse(firstBeat.narration("A").getString("reply")
        .equals(firstBeat.narration("B").getString("reply")));
    assertEquals("after-1", firstBeat.afterWorldHash);
    assertTrue(chain.consume(firstBeat));
    assertFalse(chain.consume(firstBeat));
    assertNull(chain.current("before-1", first));
    HiddenNarrativeChain.Beat secondBeat = chain.current("after-1", second);
    assertNotNull(secondBeat);
    assertEquals("after-2", secondBeat.afterWorldHash);
    assertFalse(chain.consume(secondBeat));
    assertEquals(0, chain.remaining());
  }

  @Test public void staleWorldOrAlteredChoicesCannotReusePreparedNarration() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi trước mặt", "Lần theo tiếng gió từ phía sau");
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("old-hash", "next-hash")));
    JSONObject generated = new JSONObject().put("steps", new JSONArray().put(
        draftStep("Bức tường ẩm ướt kéo dài trước mắt Cao Minh.",
            "Một luồng gió lạnh đưa Cao Minh đến khoảng sáng phía trước.",
            choices("Quan sát những vệt ố trên tường", "Lắng nghe âm thanh từ bên kia"))));
    HiddenNarrativeChain chain = HiddenNarrativeChain.parse(forecast, generated, first);
    assertNull(chain.current("new-hash", first));
    assertNull(chain.current("old-hash",
        choices("Kiểm tra một căn phòng", "Lần theo tiếng gió từ phía sau")));
    assertNotNull(chain.current("old-hash", first));
  }

  @Test public void rejectsSingleChoiceAndNonNarrativeReply() throws Exception {
    JSONArray first = choices("Kiểm tra lối đi trước mặt", "Lần theo tiếng gió từ phía sau");
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("before", "after")));
    JSONObject malformed = new JSONObject().put("steps", new JSONArray().put(
        draftStep("Bức tường ẩm ướt kéo dài trước mắt Cao Minh.",
            "Một luồng gió lạnh đưa Cao Minh đến khoảng sáng phía trước.",
            new JSONArray().put(new JSONObject().put("text", "Quan sát vết ố")))));
    boolean rejected = false;
    try { HiddenNarrativeChain.parse(forecast, malformed, first); }
    catch (IllegalArgumentException expected) { rejected = true; }
    assertTrue(rejected);

    JSONObject report = new JSONObject().put("steps", new JSONArray().put(
        draftStep("SYSTEM: world state committed.",
            "Một luồng gió lạnh đưa Cao Minh đến khoảng sáng phía trước.",
            choices("Quan sát những vệt ố trên tường", "Lắng nghe âm thanh từ bên kia"))));
    rejected = false;
    try { HiddenNarrativeChain.parse(forecast, report, first); }
    catch (IllegalArgumentException expected) { rejected = true; }
    assertTrue(rejected);
  }
}
