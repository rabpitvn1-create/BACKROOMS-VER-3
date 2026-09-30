package com.rabpit.backroom.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class NarrationGuardTest {
  @Test public void rejectsAuthoritativeFields() throws Exception {
    JSONObject generated = new JSONObject()
        .put("reply", "Cao Minh đứng yên.")
        .put("transitionTarget", "1")
        .put("choices", new JSONArray())
        .put("encounterDialogue", new JSONArray());
    assertFalse(NarrationGuard.validate(generated, new JSONObject()).isEmpty());
  }

  @Test public void rejectsDialogueWithoutCommittedEncounter() throws Exception {
    JSONObject generated = new JSONObject()
        .put("reply", "Có tiếng nói.")
        .put("choices", new JSONArray())
        .put("encounterDialogue", new JSONArray().put("Xin chào.").put("Ai đó?"));
    assertFalse(NarrationGuard.validate(generated, new JSONObject()).isEmpty());
  }

  @Test public void acceptsBoundedDialogueForPendingEncounter() throws Exception {
    JSONObject state = new JSONObject()
        .put("characterEncounter", new JSONObject()
            .put("pendingIntro", new JSONArray().put("luc_tram")));
    JSONObject generated = new JSONObject()
        .put("reply", "Hai người đối mặt.")
        .put("choices", new JSONArray())
        .put("encounterDialogue", new JSONArray().put("Ma đầu.").put("Lục tiên tử."));
    assertTrue(NarrationGuard.validate(generated, state).isEmpty());
  }

  @Test public void rejectsChoicesDuringEntityEncounter() throws Exception {
    JSONObject state = new JSONObject()
        .put("flags", new JSONObject().put("entityEncounterKey", "hound"));
    JSONObject generated = new JSONObject()
        .put("reply", "Hound áp sát.")
        .put("choices", new JSONArray().put(new JSONObject().put("text", "Đi tiếp")))
        .put("encounterDialogue", new JSONArray());
    assertFalse(NarrationGuard.validate(generated, state).isEmpty());
  }

  private static JSONObject narration(String reply, String... choices) throws Exception {
    JSONArray suggestions = new JSONArray();
    for (String choice : choices) suggestions.put(new JSONObject().put("text", choice));
    return new JSONObject().put("reply", reply).put("choices", suggestions)
        .put("encounterDialogue", new JSONArray());
  }

  @Test public void rejectsReportButAcceptsTheSameEventAsNarration() throws Exception {
    JSONObject state = new JSONObject();
    assertFalse(NarrationGuard.validate(narration(
        "Lối đi vòng lại điểm ban đầu. Tiến độ tìm lối ra bắt đầu lại."), state).isEmpty());
    assertTrue(NarrationGuard.validate(narration(
        "Qua vài khúc ngoặt, Cao Minh lại thấy vết ố cạnh chân tường. "
            + "Dãy tường vàng quen thuộc khép lối đi thành một vòng kín."), state).isEmpty());
  }

  @Test public void rejectsMechanicalEndingMetaAndRepeatedSentence() throws Exception {
    JSONObject state = new JSONObject();
    assertFalse(NarrationGuard.validate(narration(
        "Cao Minh dừng ở ngã rẽ. Bạn sẽ làm gì tiếp?"), state).isEmpty());
    assertFalse(NarrationGuard.validate(narration(
        "Trạng thái đã thay đổi: nhiệm vụ bắt đầu lại."), state).isEmpty());
    assertFalse(NarrationGuard.validate(narration(
        "DEBUG: stateDelta đã kích hoạt."), state).isEmpty());
    assertFalse(NarrationGuard.validate(narration(
        "Tiếng đèn rền lên. Tiếng đèn rền lên."), state).isEmpty());
    assertFalse(NarrationGuard.validate(narration(
        "Cao Minh quyết định quay lại hành lang."), state, "Quan sát cửa").isEmpty());
  }

  @Test public void rejectsUnprovenSafetyAndUntrackedPermanentChoice() throws Exception {
    JSONObject ordinary = new JSONObject();
    assertFalse(NarrationGuard.validate(narration(
        "Cao Minh nhận ra khu vực này hoàn toàn an toàn và không còn gì đáng ngại."),
        ordinary).isEmpty());
    assertTrue(NarrationGuard.validate(narration(
        "Cao Minh chưa tìm thấy dấu hiệu rõ ràng nào chứng minh nơi này an toàn."),
        ordinary).isEmpty());
    String scene = "Một tiếng động ngắt quãng vang lên từ phía góc phòng còn khuất.";
    assertFalse(NarrationGuard.validate(narration(scene,
        "Đánh dấu đường cũ trước khi bước tiếp"), ordinary).isEmpty());
    assertTrue(NarrationGuard.validate(narration(scene,
        "Quan sát lối đi vừa đi qua"), ordinary).isEmpty());
  }

  @Test public void postDefeatLocationMustRemainConcealed() throws Exception {
    JSONObject hidden = new JSONObject().put("perceptionShroud", true)
        .put("location", "Hành lang thực tế mà Cao Minh không nhận ra");
    assertFalse(NarrationGuard.validate(narration(
        "Cao Minh đã quay về Level 0 và nhận ra những thứ trước mắt."), hidden).isEmpty());
    assertFalse(NarrationGuard.validate(narration(
        "Cao Minh nhận ra Hành lang thực tế mà Cao Minh không nhận ra."), hidden).isEmpty());
    assertTrue(NarrationGuard.validate(narration(
        "Ánh đèn chập chờn nhưng không một dấu mốc nào đủ rõ để định hướng."),
        hidden).isEmpty());
  }

  @Test public void rejectsGenericDuplicateAndRepeatedActionChoices() throws Exception {
    String reply = "Cao Minh dừng dưới ánh đèn chập chờn, trước mặt là hai lối đi.";
    assertFalse(NarrationGuard.validate(narration(reply,
        "Tiếp tục khám phá Level 0"), new JSONObject()).isEmpty());
    assertFalse(NarrationGuard.validate(narration(reply,
        "Quan sát cánh cửa", "Quan sát cánh cửa."), new JSONObject()).isEmpty());
    assertFalse(NarrationGuard.validate(narration(reply,
        "Kiểm tra cánh cửa bên trái"), new JSONObject(),
        "Cao Minh kiểm tra cánh cửa bên trái").isEmpty());
    assertFalse(NarrationGuard.validate(narration(reply,
        "Cao Minh mở cửa và phát hiện lối ra"), new JSONObject()).isEmpty());
  }

  @Test public void acceptsTwoDistinctActionsAndSanitizesLegacySaves() throws Exception {
    String reply = "Ánh đèn rung nhẹ trên trần, còn vệt nước dưới chân tường kéo về phía ngã rẽ.";
    assertTrue(NarrationGuard.validate(narration(reply, "Lắng nghe phía sau bức tường"),
        new JSONObject(), "Đứng quan sát").isEmpty());
    assertTrue(NarrationGuard.validate(narration(reply,
        "Kiểm tra vệt nước", "Rẽ vào hành lang bên trái"), new JSONObject()).isEmpty());
    assertFalse(NarrationGuard.validate(narration(reply,
        "Kiểm tra vệt nước", "Rẽ vào hành lang bên trái", "Lắng nghe tiếng đèn"), new JSONObject()).isEmpty());
    JSONArray previous = new JSONArray().put(new JSONObject().put("text", "Kiểm tra vệt nước"))
        .put(new JSONObject().put("text", "Lắng nghe tiếng đèn"))
        .put(new JSONObject().put("text", "Rẽ vào hành lang"));
    assertEquals(2, GmChoiceContract.sanitizeChoices(previous).length());
  }

  @Test public void filtersInvalidPrefetchBranchIndependently() throws Exception {
    String valid = "Ánh đèn rung nhẹ trên trần, Cao Minh nhìn thấy một vệt nước bên tường.";
    JSONObject branches = new JSONObject().put("A", narration(valid, "Kiểm tra vệt nước"))
        .put("B", narration("Tiến độ tìm lối ra bắt đầu lại."))
        .put("C", narration(valid, "Tiếp tục khám phá Level 0"));
    Map<String, JSONObject> states = new LinkedHashMap<>();
    Map<String, String> actions = new LinkedHashMap<>();
    for (String id : new String[]{"A", "B", "C"}) {
      states.put(id, new JSONObject());
      actions.put(id, "Đứng quan sát");
    }
    Map<String, JSONObject> accepted = NarrationGuard.validPrefetchBranches(branches, states, actions);
    assertEquals(1, accepted.size());
    assertTrue(accepted.containsKey("A"));
  }

  @Test public void regeneratesOnlyAfterFailureAndValidatesAgain() throws Exception {
    JSONObject state = new JSONObject();
    String[] error = {""};
    int[] calls = {0};
    JSONObject accepted = NarrationGuard.regenerateIfInvalid(
        narration("Tiến độ tìm lối ra bắt đầu lại."), state, "Quan sát", violation -> {
          calls[0]++;
          error[0] = violation;
          return narration("Cao Minh bắt gặp vết ố quen thuộc trên tường. Lối đi đã đưa hắn trở lại ngã rẽ.");
        });
    assertEquals(1, calls[0]);
    assertFalse(error[0].isEmpty());
    assertTrue(NarrationGuard.validate(accepted, state, "Quan sát").isEmpty());

    try {
      NarrationGuard.regenerateIfInvalid(narration("Tiến độ tìm lối ra bắt đầu lại."),
          state, "Quan sát", violation -> narration("Trạng thái đã thay đổi."));
      throw new AssertionError("Invalid regeneration must not be displayed");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Narration validation failed"));
    }
  }

  @Test public void levelZeroKnowledgeAndCommittedRouteResetStayInNarrativeContract() throws Exception {
    Path knowledge = Paths.get("src/main/assets/knowledge/level_knowledge.json");
    if (!Files.isRegularFile(knowledge)) knowledge = Paths.get("app/src/main/assets/knowledge/level_knowledge.json");
    LevelCore level = LevelCore.withKnowledge(
        new String(Files.readAllBytes(knowledge), StandardCharsets.UTF_8), bound -> 0);
    JSONObject state = new JSONObject().put("turn", 1).put("currentLevelKey", "0")
        .put("currentLevel", 0).put("location", LevelCore.LEVEL_ZERO_START_LOCATION);
    level.normalizeState(state);
    state.put("turn", 2);
    level.rollRouteForExplorerAction(state, "Đi theo hành lang", bound -> 20);
    state.put("turn", 3);
    level.rollRouteForExplorerAction(state, "Đi theo hành lang", bound -> 150);

    String context = level.promptContext(state, "Đi theo hành lang");
    assertTrue(context.contains("LEVEL KNOWLEDGE BUNDLE"));
    assertTrue(context.contains("OUTCOME THIS TURN: RESET"));
    assertTrue(context.contains("giấy dán tường"));
    assertFalse(NarrationGuard.validate(narration(
        "Lối đi vòng lại điểm ban đầu. Tiến độ tìm lối ra bắt đầu lại."), state,
        "Đi theo hành lang").isEmpty());
    assertTrue(NarrationGuard.validate(narration(
        "Qua vài khúc ngoặt, Cao Minh lại thấy vệt ố cạnh chân tường. "
            + "Dãy tường vàng quen thuộc hiện ra dưới tiếng đèn rền đều.",
        "Kiểm tra vệt ố"),
        state, "Đi theo hành lang").isEmpty());
  }
}
