package com.rabpit.backroom;

import com.rabpit.backroom.core.GmChoiceContract;
import com.rabpit.backroom.core.NarrationGuard;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Experimental read-only narration cache. Every branch shares its beat's ONE
 * canonical Core outcome; only the presentation differs. Never stores or commits Core state.
 */
final class HiddenNarrativeChain {
  static final class Beat {
    final String canonicalAction, beforeWorldHash, afterWorldHash;
    final String replyA, replyB;
    final JSONArray expectedChoices, nextChoices;

    Beat(JSONObject forecast, JSONObject draft, JSONArray expected) throws Exception {
      canonicalAction = forecast.getString("canonicalAction");
      beforeWorldHash = forecast.getString("beforeWorldHash");
      afterWorldHash = forecast.getString("afterWorldHash");
      replyA = draft.getString("replyA").trim();
      replyB = draft.getString("replyB").trim();
      expectedChoices = GmChoiceContract.sanitizeChoices(expected);
      nextChoices = GmChoiceContract.sanitizeChoices(draft.getJSONArray("nextChoices"));
      if (expectedChoices.length() != 2 || nextChoices.length() != 2
          || replyA.isEmpty() || replyB.isEmpty())
        throw new IllegalArgumentException("Each chain beat needs two real choices and two replies");
      JSONObject state = forecast.getJSONObject("state");
      for (int i = 0; i < 2; i++) {
        JSONObject candidate = new JSONObject()
            .put("reply", i == 0 ? replyA : replyB)
            .put("choices", new JSONArray(nextChoices.toString()))
            .put("encounterDialogue", new JSONArray());
        String action = expectedChoices.getJSONObject(i).getString("action");
        String violation = NarrationGuard.validate(candidate, state, action);
        if (!violation.isEmpty())
          throw new IllegalArgumentException("Invalid chain beat variant " + i + ": " + violation);
      }
    }

    JSONObject narration(String choiceId) throws Exception {
      if (!"A".equals(choiceId) && !"B".equals(choiceId))
        throw new IllegalArgumentException("Invalid chain choice");
      return new JSONObject().put("reply", "A".equals(choiceId) ? replyA : replyB)
          .put("choices", new JSONArray(nextChoices.toString()))
          .put("encounterDialogue", new JSONArray());
    }
  }

  private final List<Beat> beats = new ArrayList<>();
  private int cursor;

  static HiddenNarrativeChain parse(JSONObject forecast, JSONObject generated,
                                    JSONArray firstChoices) throws Exception {
    JSONArray predictions = forecast.getJSONArray("steps");
    JSONArray drafts = generated.getJSONArray("steps");
    if (predictions.length() == 0 || predictions.length() != drafts.length()
        || predictions.length() > 4) throw new IllegalArgumentException("Chain length mismatch");
    HiddenNarrativeChain result = new HiddenNarrativeChain();
    JSONArray expected = GmChoiceContract.sanitizeChoices(firstChoices);
    for (int i = 0; i < predictions.length(); i++) {
      Beat beat = new Beat(predictions.getJSONObject(i), drafts.getJSONObject(i), expected);
      result.beats.add(beat);
      expected = beat.nextChoices;
    }
    return result;
  }

  synchronized Beat current(String worldHash, JSONArray visibleChoices) {
    if (cursor >= beats.size()) return null;
    Beat beat = beats.get(cursor);
    if (!beat.beforeWorldHash.equals(worldHash)) return null;
    JSONArray actual;
    try {
      actual = GmChoiceContract.sanitizeChoices(visibleChoices);
      if (actual.length() != 2) return null;
      for (int i = 0; i < 2; i++) {
        // The UI may display a descriptive label while submitting an explicit
        // Core action (for example, exit label -> "Đi qua lối ra").
        JSONObject source = visibleChoices.optJSONObject(i);
        String submittedAction = source == null ? actual.getJSONObject(i).getString("action")
            : source.optString("action", actual.getJSONObject(i).getString("action")).trim();
        if (!beat.expectedChoices.getJSONObject(i).getString("action")
            .equals(submittedAction)) return null;
      }
    } catch (Exception error) { return null; }
    return beat;
  }

  synchronized boolean consume(Beat beat) {
    if (cursor >= beats.size() || beats.get(cursor) != beat) return false;
    cursor++;
    return cursor < beats.size();
  }

  synchronized int remaining() { return beats.size() - cursor; }
}
