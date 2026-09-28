package com.rabpit.backroom.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class GmBranchBatchTest {
  private static JSONObject narration(String reply) throws Exception {
    return new JSONObject().put("reply", reply)
        .put("choices", new JSONArray()).put("encounterDialogue", new JSONArray());
  }

  @Test public void oneProviderRequestContainsThreeCorePreviewsAndKeepsValidBranches() throws Exception {
    Map<String, String> actions = new LinkedHashMap<>();
    Map<String, JSONObject> previews = new LinkedHashMap<>();
    for (String id : new String[]{"A", "B", "C"}) {
      actions.put(id, "Hành động " + id);
      JSONObject state = new JSONObject().put("turn", 4);
      if (id.equals("B")) state.put("characterEncounter",
          new JSONObject().put("pendingIntro", new JSONArray().put("person")));
      previews.put(id, new JSONObject().put("state", state).put("outcomeHash", "hash-" + id));
    }
    String unchanged = previews.get("A").toString();
    AtomicInteger calls = new AtomicInteger();
    JSONObject response = new JSONObject().put("branches", new JSONObject()
        .put("A", narration("Bóng đèn vừa tắt phía sau."))
        .put("B", narration("Một người hiện ra trong bóng tối."))
        .put("C", narration("Lối đi phía trước kéo dài.")));
    Map<String, JSONObject> cached = GmBranchBatch.generate(
        "A/B/C hypothetical Core previews", actions, previews, prompt -> {
          calls.incrementAndGet();
          assertTrue(prompt.contains("A/B/C"));
          return response;
        });
    assertEquals(1, calls.get());
    assertTrue(cached.containsKey("A"));
    assertFalse(cached.containsKey("B"));
    assertTrue(cached.containsKey("C"));
    assertEquals(unchanged, previews.get("A").toString());
    assertEquals("hash-C", previews.get("C").getString("outcomeHash"));
  }

  @Test public void malformedOrMissingOneBranchDoesNotDiscardOthers() throws Exception {
    Map<String, String> actions = new LinkedHashMap<>();
    Map<String, JSONObject> previews = new LinkedHashMap<>();
    for (String id : new String[]{"A", "B", "C"}) {
      actions.put(id, "Hành động " + id);
      previews.put(id, new JSONObject().put("state", new JSONObject()));
    }
    JSONObject response = new JSONObject().put("branches", new JSONObject()
        .put("A", narration("Bóng đèn vừa tắt."))
        .put("B", "invalid")
        .put("C", narration("Cánh cửa khép lại.")));
    Map<String, JSONObject> cached = GmBranchBatch.generate("batch", actions, previews, prompt -> response);
    assertEquals(2, cached.size());
    assertFalse(cached.containsKey("B"));
  }

  @Test public void narrationAndChoicesUseDifferentQualityRules() throws Exception {
    JSONObject state = new JSONObject();
    assertFalse(NarrationGuard.validate(narration("Tiến độ tìm lối ra bắt đầu lại."), state).isEmpty());
    JSONObject duplicate = narration("Bóng đèn kêu lách tách.")
        .put("choices", new JSONArray().put(new JSONObject().put("text", "Kiểm tra cửa"))
            .put(new JSONObject().put("text", "Kiểm tra cửa")));
    assertFalse(NarrationGuard.validate(duplicate, state).isEmpty());
    JSONObject repeat = narration("Bóng đèn kêu lách tách.")
        .put("choices", new JSONArray().put(new JSONObject().put("text", "Kiểm tra cửa")));
    assertFalse(NarrationGuard.validate(repeat, state, "Kiểm tra cửa").isEmpty());
    assertTrue(NarrationGuard.validate(narration("Bóng đèn kêu lách tách."), state).isEmpty());
  }
}
