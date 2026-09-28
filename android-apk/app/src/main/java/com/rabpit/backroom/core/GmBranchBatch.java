package com.rabpit.backroom.core;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.LinkedHashMap;
import java.util.Map;

/** One provider request, then independent validation of each hypothetical Core outcome. */
public final class GmBranchBatch {
  private GmBranchBatch() {}

  public interface Provider {
    JSONObject request(String prompt) throws Exception;
  }

  public static Map<String, JSONObject> generate(String prompt, Map<String, String> actions,
                                                  Map<String, JSONObject> previews,
                                                  Provider provider) throws Exception {
    JSONObject response = provider.request(prompt);
    JSONObject branches = response == null ? null : response.optJSONObject("branches");
    Map<String, JSONObject> valid = new LinkedHashMap<>();
    if (branches == null || response.length() != 1) return valid;
    for (String id : actions.keySet()) {
      JSONObject generated = branches.optJSONObject(id);
      JSONObject preview = previews.get(id);
      if (generated == null || preview == null || generated.length() != 3
          || !(generated.opt("choices") instanceof JSONArray)
          || !(generated.opt("encounterDialogue") instanceof JSONArray)
          || generated.optString("reply", "").length() > 1800) continue;
      if (NarrationGuard.validate(generated, preview.getJSONObject("state"), actions.get(id)).isEmpty())
        valid.put(id, generated);
    }
    return valid;
  }
}
