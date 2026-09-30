package com.rabpit.backroom.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Small deterministic boundary around non-authoritative narration.
 * It protects Core ownership and explicit player refusal; literary quality stays with the narrator.
 */
public final class NarrationGuard {
  private static final Set<String> FORBIDDEN_ROOT_KEYS = new HashSet<>(Arrays.asList(
      "transitionTarget", "sceneLabel", "state", "stateDelta", "currentLevel",
      "currentLevelKey", "location", "flags", "inventory", "party", "combat",
      "facts", "historicalFacts", "beliefs", "threads", "threadRegistry",
      "narrativeSkeleton", "emergent"));

  private static final Pattern SYSTEM_LEAK = Pattern.compile(
      "(?iu)(?:\\b(?:debug|stateDelta|JSON|prompt|token|API)\\b|"
          + "(?:hệ thống|game master|core|system)\\s*[:=]|\\[(?:system|debug|core)\\])");
  private static final Pattern EXPLICIT_STAY_OR_REFUSAL = Pattern.compile(
      "(?iu)(?:(?:không|chưa|từ chối)(?:\\s+\\p{L}+){0,3}\\s+"
          + "(?:đi|bước|tiến|rời|di\\s+chuyển)|"
          + "\\bngồi\\s+yên\\b|\\bđứng\\s+yên\\b|"
          + "\\báp\\s+tai\\b.{0,40}\\blắng\\s+nghe\\b|"
          + "\\bchỉ\\b.{0,40}\\blắng\\s+nghe\\b)");
  private static final Pattern NARRATED_MOVEMENT = Pattern.compile(
      "(?iu)(?:cao minh|hắn|anh)\\s+(?:tự\\s+)?(?:đứng\\s+dậy|bước|đi|chạy|bò|"
          + "tiến|rẽ|leo|di\\s+chuyển|rời|quay\\s+gót|men\\s+theo)\\b");
  private static final Pattern EXPOSED_LEVEL = Pattern.compile("(?iu)\\bLevel\\s*\\d+\\b");

  private NarrationGuard() {}

  public static String validate(JSONObject generated, JSONObject committedState) {
    return validate(generated, committedState, "");
  }

  /** Empty string means the payload stays inside the hard gameplay boundary. */
  public static String validate(JSONObject generated, JSONObject committedState, String playerAction) {
    if (generated == null) return "Narration payload is missing.";
    String reply = generated.optString("reply", "").trim();
    if (reply.isEmpty()) return "reply is required.";

    for (String key : FORBIDDEN_ROOT_KEYS) {
      if (generated.has(key)) return "Narration attempted authoritative field: " + key;
    }

    JSONArray dialogue = generated.optJSONArray("encounterDialogue");
    int dialogueCount = dialogue == null ? 0 : dialogue.length();
    int pendingCount = pendingEncounterCount(committedState);
    if (pendingCount == 0 && dialogueCount != 0) {
      return "encounterDialogue is forbidden without a committed pending encounter.";
    }
    if (pendingCount > 0 && (dialogueCount < 2 || dialogueCount > 5)) {
      return "Committed character encounter requires 2-5 dialogue lines.";
    }
    if (dialogue != null) {
      for (int i = 0; i < dialogue.length(); i++) {
        if (dialogue.optString(i, "").trim().isEmpty()) {
          return "encounterDialogue cannot contain empty lines.";
        }
      }
    }

    JSONArray choices = generated.optJSONArray("choices");
    if (hasActiveEntityEncounter(committedState) && choices != null && choices.length() > 0) {
      return "choices are forbidden while a committed Entity encounter is active.";
    }

    if (SYSTEM_LEAK.matcher(reply).find()) return "reply exposes internal game/system language.";

    String action = playerAction == null ? "" : playerAction;
    if (EXPLICIT_STAY_OR_REFUSAL.matcher(action).find()
        && NARRATED_MOVEMENT.matcher(reply).find()) {
      return "reply contradicts an explicit stationary/refusal Player Action.";
    }

    if (committedState != null && committedState.optBoolean("perceptionShroud", false)) {
      String location = committedState.optString("location", "");
      if (EXPOSED_LEVEL.matcher(reply).find()
          || (!location.isEmpty() && reply.contains(location))) {
        return "reply reveals the concealed post-defeat location.";
      }
    }

    return validateChoices(choices);
  }

  private static String validateChoices(JSONArray choices) {
    if (choices == null) return "";
    if (choices.length() > 2) return "choices must contain at most 2 suggestions.";
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < choices.length(); i++) {
      JSONObject choice = choices.optJSONObject(i);
      String text = choice == null ? "" : choice.optString("text", "").trim();
      if (text.isEmpty()) return "choice " + (char)('A' + i) + " must contain text.";
      if (text.length() > 140) return "choice " + (char)('A' + i) + " is too long.";
      if (SYSTEM_LEAK.matcher(text).find()) return "choice contains internal game/system language.";
      String normalized = normalize(text);
      if (!seen.add(normalized)) return "choices must be distinct.";
    }
    return "";
  }

  private static String normalize(String value) {
    return (value == null ? "" : value).toLowerCase(Locale.ROOT)
        .replaceAll("[^\\p{L}\\p{N}]+", " ").trim().replaceAll("\\s+", " ");
  }

  @FunctionalInterface public interface Regenerator {
    JSONObject regenerate(String violation) throws Exception;
  }

  /** One bounded retry for hard contract failures; it is not a prose-quality scorer. */
  public static JSONObject regenerateIfInvalid(JSONObject generated, JSONObject state,
                                                String action, Regenerator regenerator) throws Exception {
    String violation = validate(generated, state, action);
    if (violation.isEmpty()) return generated;
    JSONObject rewritten = regenerator.regenerate(violation);
    violation = validate(rewritten, state, action);
    if (!violation.isEmpty()) {
      throw new IllegalArgumentException("Narration validation failed: " + violation);
    }
    return rewritten;
  }

  private static int pendingEncounterCount(JSONObject state) {
    JSONObject encounter = state == null ? null : state.optJSONObject("characterEncounter");
    JSONArray pending = encounter == null ? null : encounter.optJSONArray("pendingIntro");
    return pending == null ? 0 : pending.length();
  }

  private static boolean hasActiveEntityEncounter(JSONObject state) {
    JSONObject flags = state == null ? null : state.optJSONObject("flags");
    return flags != null && !flags.optString("entityEncounterKey", "").trim().isEmpty();
  }
}
