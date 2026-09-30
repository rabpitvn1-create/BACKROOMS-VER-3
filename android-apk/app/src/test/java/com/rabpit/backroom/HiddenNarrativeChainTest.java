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
    assertEquals("before-1", firstBeat.beforeWorldHash);
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

  @Test public void exitDisplayLabelCanDifferFromCanonicalSubmittedAction() throws Exception {
    JSONArray submittedActions = choices("Đi qua lối ra",
        "Thận trọng tiếp cận lối thoát vừa tìm thấy");
    JSONArray displayed = new JSONArray()
        .put(new JSONObject().put("text", "Đi qua lối ra đến chặng kế tiếp")
            .put("action", "Đi qua lối ra"))
        .put(new JSONObject().put("text", "Thận trọng tiếp cận lối thoát vừa tìm thấy")
            .put("action", "Thận trọng tiếp cận lối thoát vừa tìm thấy"));
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("at-exit", "next-level")));
    JSONObject draft = new JSONObject().put("steps", new JSONArray()
        .put(draftStep("Cao Minh băng qua ngưỡng cửa, bước vào một khoảng sáng xa lạ.",
            "Luồng khí lạnh dẫn Cao Minh tới một gian phòng chưa từng thấy.",
            choices("Quan sát dấu vết phía trước", "Lắng nghe phía sau bức tường"))));
    HiddenNarrativeChain chain = HiddenNarrativeChain.parse(forecast, draft, submittedActions);
    assertNotNull(chain.current("at-exit", displayed));
    displayed.getJSONObject(0).put("action", "Quay về Level trước");
    assertNull(chain.current("at-exit", displayed));
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
        draftStep("Hệ thống: cập nhật trạng thái thành công.",
            "Một luồng gió lạnh đưa Cao Minh đến khoảng sáng phía trước.",
            choices("Quan sát những vệt ố trên tường", "Lắng nghe âm thanh từ bên kia"))));
    rejected = false;
    try { HiddenNarrativeChain.parse(forecast, report, first); }
    catch (IllegalArgumentException expected) { rejected = true; }
    assertTrue(rejected);
  }
  @Test public void missingForecastPreservesFreeformButConvergesTwoButtons()
      throws Exception {
    JSONArray displayed = choices("Lần theo khe hở bên trái", "Quan sát đèn trên trần");
    assertEquals("Tôi ngồi yên", HiddenNarrativeChain.resolveCoreAction(
        "Tôi ngồi yên", null, false, null, displayed));
    assertEquals("Lần theo khe hở bên trái", HiddenNarrativeChain.resolveCoreAction(
        "Quan sát đèn trên trần", "B", false, null, displayed));
    assertEquals("Đi qua lối ra", HiddenNarrativeChain.resolveCoreAction(
        "Đi qua lối ra", null, true, null, displayed));
  }

  @Test public void forecastConvergesNarrativeVariantsButNotMechanicalActions()
      throws Exception {
    JSONArray displayed = choices("Lần theo khe hở bên trái", "Quan sát đèn trên trần");
    JSONObject forecast = new JSONObject().put("steps", new JSONArray()
        .put(forecastStep("old", "new")));
    JSONObject draft = new JSONObject().put("steps", new JSONArray()
        .put(draftStep("Cao Minh tiến vào dải sáng hẹp kéo dài trước mắt.",
            "Những tiếng rung nhỏ dội từ trần khiến Cao Minh chú ý.",
            choices("Kiểm tra khu vực phía trước", "Lắng nghe từ phía sau"))));
    HiddenNarrativeChain.Beat beat = HiddenNarrativeChain.parse(forecast,draft,displayed)
        .current("old",displayed);
    assertNotNull(beat);
    assertEquals("Tiếp tục khám phá", HiddenNarrativeChain.resolveCoreAction(
        "Quan sát đèn trên trần", "B", false, beat, displayed));
    assertEquals("Tiếp tục khám phá", HiddenNarrativeChain.resolveCoreAction(
        "Tôi bò về phía ngược lại", null, false, beat, displayed));
    assertEquals("Đi qua lối ra", HiddenNarrativeChain.resolveCoreAction(
        "Đi qua lối ra", null, true, beat, displayed));
  }
}
