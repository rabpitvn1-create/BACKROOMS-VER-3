package com.rabpit.backroom.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class NarrationGuardTest {
  private static JSONObject narration(String reply, String... choices) throws Exception {
    JSONArray suggestions = new JSONArray();
    for (String choice : choices) suggestions.put(new JSONObject().put("text", choice));
    return new JSONObject().put("reply", reply).put("choices", suggestions)
        .put("encounterDialogue", new JSONArray());
  }

  @Test public void rejectsAuthoritativeFieldsAndSystemLeak() throws Exception {
    JSONObject stateWrite = narration("Cao Minh đứng trong hành lang.")
        .put("transitionTarget", "1");
    assertFalse(NarrationGuard.validate(stateWrite, new JSONObject()).isEmpty());
    assertFalse(NarrationGuard.validate(
        narration("DEBUG: stateDelta đã cập nhật."), new JSONObject()).isEmpty());
  }

  @Test public void encounterDialogueRequiresCommittedEncounter() throws Exception {
    JSONObject generated = narration("Có tiếng người phía trước.");
    generated.getJSONArray("encounterDialogue").put("Xin chào.").put("Ai đó?");
    assertFalse(NarrationGuard.validate(generated, new JSONObject()).isEmpty());

    JSONObject state = new JSONObject().put("characterEncounter",
        new JSONObject().put("pendingIntro", new JSONArray().put("luc_tram")));
    assertTrue(NarrationGuard.validate(generated, state).isEmpty());
  }

  @Test public void entityEncounterOwnsItsChoices() throws Exception {
    JSONObject state = new JSONObject().put("flags",
        new JSONObject().put("entityEncounterKey", "hound"));
    assertFalse(NarrationGuard.validate(
        narration("Hound áp sát.", "Chạy sang trái"), state).isEmpty());
  }

  @Test public void explicitRefusalCannotBeNarratedAsMovement() throws Exception {
    assertFalse(NarrationGuard.validate(
        narration("Cao Minh đứng dậy rồi bước qua góc rẽ."), new JSONObject(),
        "Tôi ngồi yên và không đi tiếp").isEmpty());
    assertTrue(NarrationGuard.validate(
        narration("Tiếng rung mỏng truyền qua bức tường cạnh Cao Minh."), new JSONObject(),
        "Tôi áp tai vào tường và chỉ lắng nghe").isEmpty());
  }

  @Test public void postDefeatLocationRemainsConcealed() throws Exception {
    JSONObject state = new JSONObject().put("perceptionShroud", true)
        .put("location", "Hành lang sâu bí mật");
    assertFalse(NarrationGuard.validate(
        narration("Cao Minh nhận ra Hành lang sâu bí mật."), state).isEmpty());
    assertFalse(NarrationGuard.validate(
        narration("Cao Minh biết mình vẫn ở Level 0."), state).isEmpty());
    assertTrue(NarrationGuard.validate(
        narration("Ánh đèn chập chờn không cho Cao Minh một dấu mốc rõ ràng."), state).isEmpty());
  }

  @Test public void proseAndChoiceStyleRemainSoft() throws Exception {
    JSONObject flexible = narration(
        "Cao Minh cảm thấy căng thẳng, cào một vết nhỏ lên lớp giấy cũ rồi quan sát tiếp.",
        "Đánh dấu chỗ vừa đi qua", "Tìm một hướng khác");
    assertTrue(NarrationGuard.validate(flexible, new JSONObject(), "Quan sát khu vực").isEmpty());
    assertFalse(NarrationGuard.validate(
        narration("Cao Minh quan sát.", "Đi trái", "Đi trái"), new JSONObject()).isEmpty());
  }

  @Test public void boundedRetryRunsOnlyForHardFailure() throws Exception {
    int[] calls = {0};
    JSONObject accepted = NarrationGuard.regenerateIfInvalid(
        narration("DEBUG: stateDelta=1"), new JSONObject(), "Quan sát", violation -> {
          calls[0]++;
          return narration("Cao Minh quan sát những thay đổi trước mắt.");
        });
    assertEquals(1, calls[0]);
    assertTrue(NarrationGuard.validate(accepted, new JSONObject()).isEmpty());
  }
}
