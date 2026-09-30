package com.rabpit.backroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import com.rabpit.backroom.core.CombatChoiceEngine;
import com.rabpit.backroom.core.GameCoreFacade;
import com.rabpit.backroom.core.GmChoiceContract;
import com.rabpit.backroom.core.GmBranchBatch;
import com.rabpit.backroom.core.GmNarrativePacket;
import com.rabpit.backroom.core.CanonRetriever;
import com.rabpit.backroom.core.GmNarratorContract;
import com.rabpit.backroom.core.NarrationGuard;
import com.rabpit.backroom.core.ProviderRetryPolicy;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity {
  private static final String TAG = "BackroomMain";
  // CI baseline notes:
  // NARRATIVE VOICE:
  // Không kết mỗi reply bằng câu hỏi tu từ
  // Không tự thêm quyết định, ý định, lời nói hoặc hành động tiếp theo cho Cao Minh

  // Semantic highlight type note: type chỉ được là character, entity, item, skill, effect, location hoặc stat
  private WebView webView;
  private final ExecutorService io = Executors.newSingleThreadExecutor();
  private final ExecutorService prefetchIo = Executors.newSingleThreadExecutor();
  private final ExecutorService narrationIo = Executors.newSingleThreadExecutor();
  private final AtomicLong prefetchGeneration = new AtomicLong();
  // Debug-only quota observations; do not log keys, full prompts or model replies.
  private final AtomicLong providerHttpAttempts = new AtomicLong();
  private final AtomicBoolean turnInFlight = new AtomicBoolean();
  private volatile PrefetchCache prefetchCache;
  private volatile HiddenNarrativeChain hiddenChain;
  private volatile String activePrefetchKey;
  private volatile boolean destroyed;
  private GameCoreFacade gameCore;
  private CanonRetriever canonRetriever;
  private static final String GEMINI_MODEL = "gemini-3.8-flash";
  private static final String HAIKU_DEFAULT_BASE_URL = "https://api.anthropic.com/v1/messages";
  private static final String HAIKU_DEFAULT_MODEL = "claude-haiku-4-5-20251001";
  private static final long HAIKU_RETRY_DELAY_MS = 1_200L;
  private static final int NARRATION_DEADLINE_SECONDS = 30;
  private static final int[] RETRYABLE = {408, 429, 500, 502, 503, 504};
  private static final String GM_STYLE_EXAMPLES_ASSET = "knowledge/gm_style_examples.json";
  private String gmStyleExamplesCache;

  @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    gameCore = GameCoreFacade.create(getApplicationContext(), BuildConfig.DEBUG);
    try {
      canonRetriever = CanonRetriever.fromAssets(getApplicationContext());
    } catch (Exception error) {
      Log.e(TAG, "Canon assets failed validation", error);
    }
    webView = new WebView(this);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(true);
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        installUiScripts();
        safeApplyImmersiveFullscreen("onPageFinished");
      }
    });
    webView.addJavascriptInterface(new GameBridge(), "Android");
    setContentView(webView);
    webView.post(() -> safeApplyImmersiveFullscreen("webViewAttached"));
    webView.loadUrl("file:///android_asset/index.html");
  }

  @Override protected void onResume() {
    super.onResume();
    safeApplyImmersiveFullscreen("onResume");
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) safeApplyImmersiveFullscreen("onWindowFocusChanged");
  }

  private void safeApplyImmersiveFullscreen(String source) {
    try {
      applyImmersiveFullscreen();
    } catch (Throwable error) {
      Log.w(TAG, "Immersive fullscreen failed in " + source + "; keeping app alive.", error);
      try {
        applyLegacyFullscreenFlags();
      } catch (Throwable fallbackError) {
        Log.w(TAG, "Legacy fullscreen fallback also failed; continuing without immersive mode.", fallbackError);
      }
    }
  }

  private void applyImmersiveFullscreen() {
    Window window = getWindow();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      WindowManager.LayoutParams attributes = window.getAttributes();
      attributes.layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
      window.setAttributes(attributes);

      // Android 15+ with targetSdk 35 already enforces edge-to-edge. Re-applying the
      // deprecated decor-fits path here has caused OEM launch crashes in this project before.
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        window.setDecorFitsSystemWindows(false);
      }

      WindowInsetsController controller = window.getInsetsController();
      if (controller == null) {
        applyLegacyFullscreenFlags();
        return;
      }
      controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
      controller.setSystemBarsBehavior(
          WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
      return;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      WindowManager.LayoutParams attributes = window.getAttributes();
      attributes.layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
      window.setAttributes(attributes);
    }
    applyLegacyFullscreenFlags();
  }

  private void applyLegacyFullscreenFlags() {
    getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }

  @Override protected void onDestroy() {
    destroyed = true;
    if (gameCore != null) gameCore.close();
    prefetchGeneration.incrementAndGet();
    prefetchIo.shutdownNow();
    narrationIo.shutdownNow();
    io.shutdownNow();
    if (webView != null) webView.destroy();
    super.onDestroy();
  }

  private String readAssetText(String path) throws Exception {
    StringBuilder text = new StringBuilder();
    try (InputStream input = getAssets().open(path);
         BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) text.append(line).append('\n');
    }
    return text.toString();
  }

  private String gmStyleExamplesContext() {
    if (gmStyleExamplesCache != null) return gmStyleExamplesCache;
    try {
      JSONObject root = new JSONObject(readAssetText(GM_STYLE_EXAMPLES_ASSET));
      StringBuilder output = new StringBuilder("GM STYLE FEW-SHOT EXAMPLES:\n");
      String instruction = root.optString("instruction", "").trim();
      if (!instruction.isEmpty()) output.append(instruction).append("\n");

      JSONArray examples = root.optJSONArray("goodExamples");
      if (examples != null) {
        for (int i = 0; i < examples.length(); i++) {
          JSONObject example = examples.optJSONObject(i);
          if (example == null) continue;
          String player = example.optString("player", "").trim();
          String gm = example.optString("gm", "").trim();
          if (player.isEmpty() || gm.isEmpty()) continue;
          output.append("\nGOOD EXAMPLE ").append(i + 1).append("\n");
          output.append("PLAYER: ").append(player).append("\n");
          output.append("GM: ").append(gm).append("\n");
          JSONArray choices = example.optJSONArray("choices");
          if (choices != null) output.append("CHOICES: ").append(choices).append("\n");
        }
      }

      JSONArray badExamples = root.optJSONArray("badExamples");
      if (badExamples == null) badExamples = new JSONArray().put(root.optJSONObject("badExample"));
      for (int i = 0; i < badExamples.length(); i++) {
        JSONObject bad = badExamples.optJSONObject(i);
        if (bad == null) continue;
        String player = bad.optString("player", "").trim();
        String gm = bad.optString("gm", "").trim();
        String why = bad.optString("why", "").trim();
        if (!player.isEmpty() && !gm.isEmpty()) {
          output.append("\nBAD EXAMPLE ").append(i + 1).append(" — DO NOT IMITATE\n");
          output.append("PLAYER: ").append(player).append("\n");
          output.append("GM: ").append(gm).append("\n");
          JSONArray choices = bad.optJSONArray("choices");
          if (choices != null) output.append("BAD CHOICES: ").append(choices).append("\n");
          if (!why.isEmpty()) output.append("WHY BAD: ").append(why).append("\n");
        }
      }

      output.append("\nUse these examples only as style references. Never copy their wording, events, imagery, locations, conclusions, or hidden outcomes into the current turn unless current state independently supports them.\n");
      gmStyleExamplesCache = output.toString();
    } catch (Exception error) {
      Log.w(TAG, "Unable to load GM style examples; using narrative contract only.", error);
      gmStyleExamplesCache = "";
    }
    return gmStyleExamplesCache;
  }

  private void installUiScripts() {
    try {
      String snapshotUi = readAssetText("snapshot-ui.js");
      String gmChoiceUi = readAssetText("gm-choice-ui.js");
      String inventoryUi = readAssetText("inventory-ui.js");
      String partyUi = readAssetText("party-ui.js");
      String playerActionUi = readAssetText("player-action-ui.js");
      String managementUi = readAssetText("management-ui.js");
      webView.evaluateJavascript(snapshotUi, ignored ->
        webView.evaluateJavascript(gmChoiceUi, ignoredChoice ->
          webView.evaluateJavascript(inventoryUi, ignoredInventory ->
            webView.evaluateJavascript(partyUi, ignoredParty ->
              webView.evaluateJavascript(playerActionUi, ignoredPlayerAction ->
                webView.evaluateJavascript(managementUi, null))))));
    } catch (Exception e) {
      Log.e(TAG, "Unable to install WebView UI scripts", e);
    }
  }

  private boolean retryable(int code) {
    for (int value : RETRYABLE) if (value == code) return true;
    return false;
  }

  private String[] geminiKeys() {
    return new String[] {
      BuildConfig.GEMINI_API_KEY_1,
      BuildConfig.GEMINI_API_KEY_2,
      BuildConfig.GEMINI_API_KEY_3,
      BuildConfig.GEMINI_API_KEY_4,
      BuildConfig.GEMINI_API_KEY_5
    };
  }

  private void sleepBeforeNextGeminiKey(int keyIndex, int status) {
    if (status != 0 && !retryable(status)) return;
    long delayMs = Math.min(2_000L, 500L + (long)keyIndex * 350L);
    try {
      Thread.sleep(delayMs);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }

  private String postJson(String endpoint, String key, String authHeader, JSONObject payload) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
    connection.setRequestMethod("POST");
    connection.setConnectTimeout(20000);
    connection.setReadTimeout(60000);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty(authHeader, authHeader.equals("Authorization") ? "Bearer " + key : key);
    providerHttpAttempts.incrementAndGet();
    try (OutputStream output = connection.getOutputStream()) {
      output.write(payload.toString().getBytes("UTF-8"));
    }

    int status = connection.getResponseCode();
    InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
    StringBuilder body = new StringBuilder();
    if (stream != null) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
        String line;
        while ((line = reader.readLine()) != null) body.append(line);
      }
    }
    connection.disconnect();

    if (status < 200 || status >= 300) {
      String detail = body.length() > 220 ? body.substring(0, 220) : body.toString();
      throw new HttpError(status, "Provider HTTP " + status + (detail.isEmpty() ? "" : ": " + detail));
    }
    return body.toString();
  }

  private String geminiJson(String prompt, JSONObject config) throws Exception {
    JSONObject part = new JSONObject().put("text", prompt);
    JSONObject contents = new JSONObject().put("role", "user").put("parts", new JSONArray().put(part));
    JSONObject body = new JSONObject().put("contents", new JSONArray().put(contents))
        .put("generationConfig", config);
    Exception last = null;
    String[] keys = geminiKeys();
    boolean configured = false;
    for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Narration cancelled");
      String key = keys[keyIndex];
      if (key == null || key.trim().isEmpty()) continue;
      configured = true;
      try {
        providerHttpAttempts.incrementAndGet();
        String rawResponse = postJson(
            "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent",
            key, "x-goog-api-key", body);
        if (BuildConfig.DEBUG) {
          try {
            JSONObject usage = new JSONObject(rawResponse).optJSONObject("usageMetadata");
            if (usage != null) Log.d(TAG, "NARRATIVE_API_USAGE provider=gemini"
                + " attempts=" + providerHttpAttempts.get()
                + " input_tokens=" + usage.optInt("promptTokenCount", -1)
                + " output_tokens=" + usage.optInt("candidatesTokenCount", -1)
                + " thinking_tokens=" + usage.optInt("thoughtsTokenCount", -1)
                + " total_tokens=" + usage.optInt("totalTokenCount", -1));
          } catch (Exception ignored) { /* Telemetry cannot affect narration. */ }
        }
        String output = geminiResponseText(rawResponse);
        parseModelJson(output);
        return output;
      } catch (Exception error) {
        if (Thread.currentThread().isInterrupted()) throw error;
        last = error;
        int status = error instanceof HttpError ? ((HttpError)error).status : 0;
        if (keyIndex < keys.length - 1) sleepBeforeNextGeminiKey(keyIndex, status);
      }
    }
    if (!configured) throw new Exception("Không có Gemini API key trong APK.");
    throw last != null ? last : new Exception("Tất cả Gemini API key đều không khả dụng.");
  }

  private String geminiText(String prompt) throws Exception {
    JSONObject config = new JSONObject().put("responseMimeType", "application/json")
        .put("thinkingConfig", new JSONObject().put("thinkingLevel", "low"));
    return geminiJson(prompt, config);
  }

  private String geminiResponseText(String raw) throws Exception {
    JSONObject result = new JSONObject(raw);
    JSONArray candidates = result.optJSONArray("candidates");
    StringBuilder text = new StringBuilder();
    if (candidates != null) for (int c = 0; c < candidates.length(); c++) {
      JSONObject candidate = candidates.optJSONObject(c);
      JSONObject content = candidate == null ? null : candidate.optJSONObject("content");
      JSONArray parts = content == null ? null : content.optJSONArray("parts");
      if (parts == null) continue;
      for (int p = 0; p < parts.length(); p++) {
        JSONObject part = parts.optJSONObject(p);
        String piece = part == null ? "" : part.optString("text", "").trim();
        if (!piece.isEmpty()) {
          if (text.length() > 0) text.append('\n');
          text.append(piece);
        }
      }
    }
    if (text.length() == 0) throw new Exception("Gemini không trả nội dung.");
    return text.toString();
  }

  private JSONObject branchSchema() throws Exception {
    JSONObject choice = new JSONObject().put("type", "OBJECT")
        .put("properties", new JSONObject().put("text", new JSONObject().put("type", "STRING")))
        .put("required", new JSONArray().put("text"));
    JSONObject branch = new JSONObject().put("type", "OBJECT")
        .put("properties", new JSONObject()
            .put("reply", new JSONObject().put("type", "STRING"))
            .put("choices", new JSONObject().put("type", "ARRAY").put("items", choice))
            .put("encounterDialogue", new JSONObject().put("type", "ARRAY")
                .put("items", new JSONObject().put("type", "STRING"))))
        .put("required", new JSONArray().put("reply").put("choices").put("encounterDialogue"));
    JSONObject branches = new JSONObject().put("type", "OBJECT")
        .put("properties", new JSONObject().put("A", branch).put("B", branch).put("C", branch))
        .put("required", new JSONArray().put("A").put("B").put("C"));
    return new JSONObject().put("type", "OBJECT")
        .put("properties", new JSONObject().put("branches", branches))
        .put("required", new JSONArray().put("branches"));
  }

  /** Each configured Gemini key is tried before Haiku for the three-branch prefetch. */
  private JSONObject geminiBranchBatch(String prompt) throws Exception {
    JSONObject config = new JSONObject().put("responseMimeType", "application/json")
        .put("responseSchema", branchSchema()).put("maxOutputTokens", 8192)
        .put("thinkingConfig", new JSONObject().put("thinkingLevel", "low"));
    try {
      return parseModelJson(geminiJson(prompt, config));
    } catch (Exception geminiError) {
      Log.w(TAG, "All Gemini keys failed for branch prefetch; falling back to Haiku.");
      try {
        return parseModelJson(haikuText(prompt, 8192));
      } catch (Exception haikuError) {
        throw new Exception("Gemini: " + providerErrorSummary(geminiError)
            + " | Haiku: " + providerErrorSummary(haikuError));
      }
    }
  }

  private boolean haikuConfigured() {
    return BuildConfig.HAIKU_API != null && !BuildConfig.HAIKU_API.trim().isEmpty();
  }

  private String haikuModel() {
    String configured = BuildConfig.HAIKU_MODEL == null ? "" : BuildConfig.HAIKU_MODEL.trim();
    return configured.isEmpty() ? HAIKU_DEFAULT_MODEL : configured;
  }

  private String haikuBaseUrl() throws Exception {
    String configured = BuildConfig.HAIKU_BASE_URL == null ? "" : BuildConfig.HAIKU_BASE_URL.trim();
    String base = configured.isEmpty() ? HAIKU_DEFAULT_BASE_URL : configured;
    if (!base.toLowerCase(java.util.Locale.ROOT).startsWith("https://")) {
      throw new Exception("HAIKU_BASE_URL phải dùng HTTPS.");
    }
    while (base.endsWith("/") && base.length() > "https://".length()) {
      base = base.substring(0, base.length() - 1);
    }
    return base;
  }

  private String haikuEndpoint(String suffix) throws Exception {
    String base = haikuBaseUrl();
    if (base.endsWith(suffix)) return base;
    if (base.endsWith("/v1")) return base + suffix;
    if ("/messages".equals(suffix) && !base.contains("/v1")) return base + "/v1/messages";
    return base + suffix;
  }

  private String postJsonHaiku(String endpoint, JSONObject payload, boolean anthropic) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
    connection.setRequestMethod("POST");
    connection.setConnectTimeout(20_000);
    connection.setReadTimeout(60_000);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    if (anthropic) {
      connection.setRequestProperty("x-api-key", BuildConfig.HAIKU_API);
      connection.setRequestProperty("anthropic-version", "2023-06-01");
    } else {
      connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.HAIKU_API);
    }
    try (OutputStream output = connection.getOutputStream()) {
      output.write(payload.toString().getBytes("UTF-8"));
    }

    int status = connection.getResponseCode();
    InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
    StringBuilder body = new StringBuilder();
    if (stream != null) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
        String line;
        while ((line = reader.readLine()) != null) body.append(line);
      }
    }
    connection.disconnect();

    if (status < 200 || status >= 300) {
      String detail = body.length() > 220 ? body.substring(0, 220) : body.toString();
      throw new HttpError(status, "Haiku HTTP " + status + (detail.isEmpty() ? "" : ": " + detail));
    }
    if (BuildConfig.DEBUG) {
      try {
        JSONObject usage = new JSONObject(body.toString()).optJSONObject("usage");
        if (usage != null) Log.d(TAG, "NARRATIVE_API_USAGE provider=haiku"
            + " attempts=" + providerHttpAttempts.get()
            + " input_tokens=" + usage.optInt("input_tokens", usage.optInt("prompt_tokens", -1))
            + " output_tokens=" + usage.optInt("output_tokens", usage.optInt("completion_tokens", -1)));
      } catch (Exception ignored) { /* Telemetry cannot affect narration. */ }
    }
    return body.toString();
  }

  private String haikuAnthropicText(String prompt, int maxTokens) throws Exception {
    JSONObject body = new JSONObject()
        .put("model", haikuModel())
        .put("max_tokens", maxTokens)
        .put("temperature", 0.6)
        .put("messages", new JSONArray().put(
            new JSONObject().put("role", "user").put("content", prompt)));
    JSONObject result = new JSONObject(postJsonHaiku(haikuEndpoint("/messages"), body, true));
    JSONArray content = result.optJSONArray("content");
    StringBuilder text = new StringBuilder();
    if (content != null) {
      for (int i = 0; i < content.length(); i++) {
        JSONObject part = content.optJSONObject(i);
        String piece = part == null ? "" : part.optString("text", "").trim();
        if (!piece.isEmpty()) {
          if (text.length() > 0) text.append('\n');
          text.append(piece);
        }
      }
    }
    if (text.length() == 0) throw new Exception("Haiku không trả nội dung.");
    return text.toString();
  }

  private String haikuOpenAiText(String prompt, int maxTokens) throws Exception {
    JSONObject body = new JSONObject()
        .put("model", haikuModel())
        .put("temperature", 0.6)
        .put("max_tokens", maxTokens)
        .put("messages", new JSONArray().put(
            new JSONObject().put("role", "user").put("content", prompt)));
    JSONObject result = new JSONObject(postJsonHaiku(haikuEndpoint("/chat/completions"), body, false));
    JSONArray choices = result.optJSONArray("choices");
    if (choices == null || choices.length() == 0) throw new Exception("Haiku không trả nội dung.");
    JSONObject first = choices.optJSONObject(0);
    JSONObject message = first == null ? null : first.optJSONObject("message");
    Object rawContent = message == null ? null : message.opt("content");
    StringBuilder text = new StringBuilder();
    if (rawContent instanceof String) {
      text.append(((String)rawContent).trim());
    } else if (rawContent instanceof JSONArray) {
      JSONArray parts = (JSONArray)rawContent;
      for (int i = 0; i < parts.length(); i++) {
        JSONObject part = parts.optJSONObject(i);
        String piece = part == null ? "" : part.optString("text", "").trim();
        if (!piece.isEmpty()) {
          if (text.length() > 0) text.append('\n');
          text.append(piece);
        }
      }
    }
    if (text.length() == 0) throw new Exception("Haiku không trả nội dung.");
    return text.toString();
  }

  private boolean protocolMismatch(Exception error) {
    if (!(error instanceof HttpError)) return false;
    int status = ((HttpError)error).status;
    return status == 400 || status == 404 || status == 405 || status == 415 || status == 422;
  }

  private String haikuTextOnce(String prompt, int maxTokens) throws Exception {
    String base = haikuBaseUrl();
    String output;
    if (base.endsWith("/chat/completions")) {
      output = haikuOpenAiText(prompt, maxTokens);
    } else if (base.endsWith("/messages") || base.contains("api.anthropic.com")) {
      output = haikuAnthropicText(prompt, maxTokens);
    } else {
      try {
        output = haikuOpenAiText(prompt, maxTokens);
      } catch (Exception openAiError) {
        if (!protocolMismatch(openAiError)) throw openAiError;
        output = haikuAnthropicText(prompt, maxTokens);
      }
    }
    parseModelJson(output);
    return output;
  }

  private String haikuText(String prompt) throws Exception {
    return haikuText(prompt, 2048);
  }

  private String haikuText(String prompt, int maxTokens) throws Exception {
    if (!haikuConfigured()) throw new Exception("HAIKU_API chưa được cấu hình.");
    Exception last = null;
    for (int attempt = 0; attempt < 2; attempt++) {
      if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Narration cancelled");
      try {
        return haikuTextOnce(prompt, maxTokens);
      } catch (Exception error) {
        if (Thread.currentThread().isInterrupted()) throw error;
        last = error;
        int status = error instanceof HttpError ? ((HttpError)error).status : 0;
        if (attempt == 0 && ProviderRetryPolicy.shouldRetrySameProvider(status, error.getMessage())) {
          Log.w(TAG, "Haiku transport/server attempt failed; retrying once.");
          try {
            Thread.sleep(HAIKU_RETRY_DELAY_MS);
          } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
          }
          continue;
        }
        break;
      }
    }
    throw last != null ? last : new Exception("Haiku không khả dụng.");
  }

  private String providerErrorSummary(Exception error) {
    if (error == null) return "không xác định";
    String message = error.getMessage();
    if (message == null || message.trim().isEmpty()) return error.getClass().getSimpleName();
    return message.length() > 260 ? message.substring(0, 260) : message;
  }

  private String generateText(String prompt) throws Exception {
    Exception geminiError;
    try {
      // geminiText() rotates through GEMINI_API_KEY_1..5 before it gives up.
      return geminiText(prompt);
    } catch (Exception error) {
      if (Thread.currentThread().isInterrupted()) throw error;
      geminiError = error;
      Log.w(TAG, "All Gemini keys failed; falling back to Haiku.");
    }

    try {
      return haikuText(prompt);
    } catch (Exception haikuError) {
      throw new Exception(
          "Toàn bộ 5 Gemini key và Haiku fallback đều không khả dụng. Gemini: "
              + providerErrorSummary(geminiError)
              + " | Haiku: "
              + providerErrorSummary(haikuError));
    }
  }

  static <T> T awaitNarration(ExecutorService executor, Callable<T> task,
                              long timeoutMillis) throws Exception {
    Future<T> pending = executor.submit(task);
    try {
      return pending.get(timeoutMillis, TimeUnit.MILLISECONDS);
    } finally {
      pending.cancel(true);
    }
  }

  private JSONObject parseModelJson(String raw) throws Exception {
    if (raw == null) throw new Exception("AI không trả dữ liệu.");
    String text = raw.trim();
    if (text.startsWith("```")) {
      int firstNewline = text.indexOf('\n');
      if (firstNewline >= 0) text = text.substring(firstNewline + 1);
      int fence = text.lastIndexOf("```");
      if (fence >= 0) text = text.substring(0, fence);
      text = text.trim();
    }
    int start = text.indexOf('{');
    int end = text.lastIndexOf('}');
    if (start < 0 || end <= start) throw new Exception("AI trả JSON không hợp lệ.");
    return new JSONObject(text.substring(start, end + 1));
  }

  private String clipped(Object value, int max) {
    String text = value == null ? "" : String.valueOf(value);
    return text.length() > max ? text.substring(text.length() - max) : text;
  }

  private String recentContext(JSONObject state) {
    JSONArray log = state == null ? null : state.optJSONArray("log");
    if (log == null || log.length() == 0) return "(chưa có lượt trước)";

    java.util.ArrayList<String> visible = new java.util.ArrayList<>();
    for (int i = log.length() - 1; i >= 0 && visible.size() < 6; i--) {
      JSONObject entry = log.optJSONObject(i);
      if (entry == null) continue;
      String role = entry.optString("role", "");
      String text = entry.optString("text", "").trim();
      if (text.isEmpty()) continue;
      visible.add(0, ("player".equals(role) ? "PLAYER: " : "GM: ") + clipped(text, 680));
    }

    StringBuilder recent = new StringBuilder();
    for (String line : visible) {
      if (recent.length() > 0) recent.append('\n');
      if (recent.length() + line.length() > GmNarrativePacket.MAX_RECENT_CONTEXT_CHARS) break;
      recent.append(line);
    }
    return recent.length() == 0 ? "(chưa có lượt trước)" : recent.toString();
  }

  private String appendEncounterDialogue(String reply, JSONArray dialogue) {
    if (dialogue == null || dialogue.length() == 0) return reply;
    StringBuilder output = new StringBuilder(reply == null ? "" : reply.trim());
    for (int i = 0; i < dialogue.length(); i++) {
      String line = dialogue.optString(i, "").trim();
      if (line.isEmpty()) continue;
      if (output.length() > 0) output.append("\n\n");
      output.append(line);
    }
    return output.toString();
  }

  private String encounterKey(JSONObject state) {
    JSONObject flags = state == null ? null : state.optJSONObject("flags");
    return flags == null ? "" : flags.optString("entityEncounterKey", "").trim().toLowerCase();
  }

  private int lastGmLogIndex(JSONObject state) {
    JSONArray log = state == null ? null : state.optJSONArray("log");
    if (log == null || log.length() == 0) return 0;
    for (int i = log.length() - 1; i >= 0; i--) {
      JSONObject entry = log.optJSONObject(i);
      if (entry != null && !"player".equals(entry.optString("role"))) return i;
    }
    return Math.max(0, log.length() - 1);
  }

  private String narrationPrompt(JSONObject state, String action) throws Exception {
    String coreJson = state.toString();
    String levelContext = gameCore.levelPromptContext(coreJson, action);
    String entityContext = gameCore.entityPromptContext(coreJson);
    String itemContext = gameCore.itemPromptContext(coreJson);
    String characterContext = gameCore.characterPromptContext(coreJson);
    String levelName = levelContext.startsWith("CURRENT LEVEL NODE: ")
        ? levelContext.substring("CURRENT LEVEL NODE: ".length()).split("\\n", 2)[0] : "";
    CanonRetriever.CanonPacket canon = canonRetriever == null ? null
        : canonRetriever.retrieve(state, action, CanonRetriever.DEFAULT_BUDGET,
            BuildConfig.DEBUG, levelName);
    if (canon == null || canon.budgetExceeded) {
      Log.w(TAG, "Canon retrieval unavailable/over budget/missing refs: "
          + (canon == null ? "index unavailable" : "size=" + canon.charCount
              + " missing=" + canon.missingMandatoryRefs + " requires=" + canon.missingRefs));
      throw new IllegalStateException("Canon bắt buộc không khả dụng trong budget; không gọi AI narration.");
    }
    if (!canon.missingMandatoryRefs.isEmpty()) Log.w(TAG,
        "Markdown canon missing/conflicting; Core context remains authoritative: "
            + canon.missingMandatoryRefs);
    if (BuildConfig.DEBUG) Log.d(TAG, "CANON RETRIEVAL: " + canon.trace);
    return GmNarrativePacket.build(levelContext, entityContext, itemContext, characterContext,
        recentContext(state), state, action, gmStyleExamplesContext(), canon.promptText());
  }


  /** Mirror the exact deterministic WebView choices for cache authorization. */
  private static JSONArray initialNarrativeChoices(JSONObject state) throws Exception {
    JSONArray empty = new JSONArray();
    if (state == null) return empty;
    JSONObject combat = state.optJSONObject("combat");
    JSONObject flags = state.optJSONObject("flags");
    if (combat != null && (combat.optBoolean("active", false)
        || "defeat".equals(combat.optString("outcome", "")))) return empty;
    if (flags != null && !flags.optString("entityEncounterKey", "").isEmpty()) return empty;

    JSONArray log = state.optJSONArray("log");
    int gmIndex = lastGmLogIndexStatic(state);
    JSONObject entry = log == null || gmIndex < 0 ? null : log.optJSONObject(gmIndex);
    JSONArray current = entry == null ? null : entry.optJSONArray("choices");
    JSONArray result;
    // The opening WebView override takes precedence over any prologue choices.
    if (log != null && log.length() == 1 && entry == log.optJSONObject(0)) {
      result = new JSONArray()
          .put(new JSONObject().put("text", "Tiếp tục khám phá dãy tường vàng"))
          .put(new JSONObject().put("text", "Lần theo những âm thanh xa lạ"));
    } else if (current != null && current.length() > 0) {
      result = new JSONArray();
      for (int i = 0; i < Math.min(2, current.length()); i++)
        result.put(current.get(i));
    } else {
      boolean postVictory = combat != null && "victory".equals(combat.optString("outcome", ""))
          && combat.optInt("logIndex", -1) == gmIndex;
      String previous = "";
      if (log != null) for (int i = log.length() - 1; i >= 0; i--) {
        JSONObject item = log.optJSONObject(i);
        if (item != null && "player".equals(item.optString("role", ""))) {
          previous = item.optString("text", "").trim();
          break;
        }
      }
      String fallback = previous.matches("(?iu)^(?:cao minh\\s+)?(?:quan sát|nhìn|xem xét|kiểm tra|khảo sát)\\b.*")
          ? "Lắng nghe âm thanh trong khu vực hiện tại" : "Quan sát khu vực phía trước";
      result = postVictory
          ? new JSONArray()
              .put(new JSONObject().put("text", "Quan sát khu vực sau trận chiến"))
              .put(new JSONObject().put("text", "Tìm một lối đi khác để tiếp tục"))
          : new JSONArray()
              .put(new JSONObject().put("text", fallback))
              .put(new JSONObject().put("text", "Thử lần theo một lối đi ít dấu vết"));
    }
    if (result.length() == 1)
      result.put(new JSONObject().put("text", "Khảo sát những âm thanh khác thường gần đó"));
    JSONObject route = state.optJSONObject("levelRoute");
    if (route != null && route.optBoolean("exitAvailable", false)) {
      result = new JSONArray()
          .put(new JSONObject().put("text", "Đi qua lối ra đến chặng kế tiếp")
              .put("action", "Đi qua lối ra"))
          .put(new JSONObject().put("text", "Thận trọng tiếp cận lối thoát vừa tìm thấy"));
    }
    return result;
  }

  private static int lastGmLogIndexStatic(JSONObject state) {
    JSONArray log = state == null ? null : state.optJSONArray("log");
    if (log == null) return -1;
    for (int i = log.length() - 1; i >= 0; i--) {
      JSONObject row = log.optJSONObject(i);
      if (row != null && !"player".equals(row.optString("role", ""))) return i;
    }
    return -1;
  }

  /**
   * An entirely speculative operation. The live Core is unchanged; an invalidated
   * batch never becomes authoritative, even if Gemini returns after an input event.
   */
  private synchronized void prefetchNarrativeChain(String sourceJson, String choicesJson) {
    if (destroyed || turnInFlight.get()) return;
    final String fullHash = gameCore.currentStateHash();
    final String worldHash = gameCore.currentNarrativeWorldHash();
    final JSONObject source;
    final JSONArray choices;
    try {
      source = new JSONObject(sourceJson);
      choices = GmChoiceContract.sanitizeChoices(new JSONArray(choicesJson));
      if (choices.length() != 2) return;
      JSONObject live = new JSONObject(gameCore.currentCoreState());
      int index = lastGmLogIndexStatic(source);
      int liveIndex = lastGmLogIndexStatic(live);
      if (index < 0 || index != liveIndex || source.optInt("turn", -1) != live.optInt("turn", -2)
          || !source.getJSONArray("log").getJSONObject(index).optString("text", "")
              .equals(live.getJSONArray("log").getJSONObject(liveIndex).optString("text", "")))
        return;
      if (hiddenChain != null && hiddenChain.current(worldHash, choices) != null) return;
    } catch (Exception ignored) { return; }
    String key = fullHash + "|" + choices.toString();
    if (key.equals(activePrefetchKey)) return;
    final long generation = prefetchGeneration.incrementAndGet();
    activePrefetchKey = key;
    try {
      prefetchIo.execute(() -> {
        try {
          JSONArray authorized = initialNarrativeChoices(new JSONObject(gameCore.currentCoreState()));
          String firstAction = authorized.getJSONObject(0).optString("action",
              authorized.getJSONObject(0).getString("text"));
          JSONObject forecast = new JSONObject(
              gameCore.previewNarrativeChain(firstAction, 3, fullHash));
          JSONArray predicted = forecast.optJSONArray("steps");
          if (!forecast.optBoolean("handled", false) || predicted == null
              || predicted.length() == 0 || generation != prefetchGeneration.get()
              || !fullHash.equals(gameCore.currentStateHash())) return;

          StringBuilder prompt = new StringBuilder(
              "HIDDEN NARRATIVE CHAIN: You are planning exactly " + predicted.length()
              + " FUTURE narrative beats in ONE response. Java Core has already computed every"
              + " mandatory result for each beat. NEVER modify the result, Level, Entity,"
              + " chest, loot, party or survivor state. Each beat has TWO distinct ways"
              + " of experiencing the EXACT SAME world outcome. The two approaches must"
              + " differ in actions, emotional tone, sensory focus and causal narration,"
              + " not simply synonyms. Do not reveal convergence, hidden plans, RNG,"
              + " status reports or location knowledge unavailable to Cao Minh. "
              + " No new entity or inventory events. All player-facing text in natural Vietnamese."
              + " For beat 0, the two displayed actions are supplied below."
              + " For beat N+1, its two actions MUST be the previous beat's nextChoices."
              + " Invent 2 concrete, meaningfully different nextChoices per beat."
              + " Keep each reply under 1400 characters. If the same destination recurs,"
              + " vary sensory focus and narrative structure instead of repeatedly"
              + " depicting a corridor folding or teleportation."
              + " Return ONLY a JSON object with field steps. Each step contains"
              + " replyA and replyB (natural Vietnamese prose), plus nextChoices"
              + " as an array of exactly two objects with a text field."
              + " Output exactly one entry per supplied beat.\n");
          for (int i = 0; i < predicted.length(); i++) {
            JSONObject beat = predicted.getJSONObject(i);
            JSONObject target = beat.getJSONObject("state");
            prompt.append("\n=== CANONICAL BEAT ").append(i).append(" ===\n");
            if (i == 0) prompt.append("VISIBLE CHOICES: ").append(choices).append('\n');
            prompt.append("HIDDEN CORE ACTION (never narrate as if player selected it): ")
                .append(beat.optString("canonicalAction", "")).append('\n')
                .append("MANDATORY RESULT: location=")
                .append(target.optString("location", ""))
                .append("; Level=").append(target.optString("currentLevelKey", ""))
                .append("; route=").append(target.optJSONObject("levelRoute"))
                .append("; selected=").append(target.optJSONObject("emergent") == null ? "NONE"
                    : target.getJSONObject("emergent").optJSONObject("lastSelection"))
                .append("; replyHint=").append(beat.optString("replyHint", "")).append("\n");
            // Share the expensive narrator contract and recent history once per batch.
            // Only a real Level change needs a second full canon retrieval.
            String previousLevel = i == 0 ? "" : predicted.getJSONObject(i - 1)
                .getJSONObject("state").optString("currentLevelKey", "");
            String currentLevel = target.optString("currentLevelKey", "");
            if (i == 0 || !currentLevel.equals(previousLevel)) {
              prompt.append(narrationPrompt(target, beat.getString("canonicalAction"))).append("\n");
            } else {
              prompt.append("CORE LEVEL / ROUTE CONTEXT: ")
                  .append(gameCore.levelPromptContext(target.toString(),
                      beat.getString("canonicalAction"))).append("\n");
            }
          }
          prompt.append("\\nFINAL INSTRUCTION: Ignore embedded single-beat JSON examples."
              + " The final response must be one object with a steps array"
              + " containing exactly " + predicted.length() + " elements.\\n");
          if (BuildConfig.DEBUG) Log.d(TAG, "HIDDEN_CHAIN_BATCH prompt_chars=" + prompt.length()
              + " beats=" + predicted.length());
          long requestsBeforeBatch = providerHttpAttempts.get();
          JSONObject generated = awaitNarration(narrationIo,
              () -> parseModelJson(generateText(prompt.toString())),
              TimeUnit.SECONDS.toMillis(NARRATION_DEADLINE_SECONDS));
          if (BuildConfig.DEBUG) Log.d(TAG, "HIDDEN_CHAIN_BATCH_API"
              + " requests=" + (providerHttpAttempts.get() - requestsBeforeBatch)
              + " beats=" + predicted.length());
          HiddenNarrativeChain chain = HiddenNarrativeChain.parse(forecast, generated, choices);
          synchronized (MainActivity.this) {
            if (!destroyed && generation == prefetchGeneration.get() && !turnInFlight.get()
                && fullHash.equals(gameCore.currentStateHash())) hiddenChain = chain;
          }
        } catch (Exception error) {
          Log.w(TAG, "Hidden narrative prefetch unavailable; ordinary narration remains available: "
              + providerErrorSummary(error));
        }
      });
    } catch (java.util.concurrent.RejectedExecutionException ignored) {}
  }

  private static final class PrefetchBranch {
    final String action, outcomeHash;
    final JSONObject narration;
    PrefetchBranch(String action, String outcomeHash, JSONObject narration) {
      this.action = action;
      this.outcomeHash = outcomeHash;
      this.narration = narration;
    }
  }

  private static final class PrefetchCache {
    final String baseHash;
    final int sourceTurn, sourceGmIndex;
    final String sourceGmText;
    final Map<String, PrefetchBranch> branches;
    PrefetchCache(String baseHash, int sourceTurn, int sourceGmIndex, String sourceGmText,
                  Map<String, PrefetchBranch> branches) {
      this.baseHash = baseHash;
      this.sourceTurn = sourceTurn;
      this.sourceGmIndex = sourceGmIndex;
      this.sourceGmText = sourceGmText;
      this.branches = branches;
    }
    PrefetchBranch forChoice(String id, String action, int turn, int index, String gmText) {
      PrefetchBranch branch = branches.get(id);
      return sourceTurn == turn && sourceGmIndex == index && sourceGmText.equals(gmText)
          && branch != null && branch.action.equals(action) ? branch : null;
    }
  }

  private synchronized void invalidatePrefetch() {
    prefetchGeneration.incrementAndGet();
    prefetchCache = null;
    activePrefetchKey = null;
  }

  private synchronized void prefetchChoices(String sourceJson, String choicesJson) {
    if (destroyed || turnInFlight.get()) return;
    final String baseHash = gameCore.currentStateHash();
    final JSONObject source, current;
    final int sourceTurn, sourceGmIndex;
    final String sourceGmText;
    try {
      source = new JSONObject(sourceJson);
      current = new JSONObject(gameCore.currentCoreState());
      sourceTurn = source.getInt("turn");
      sourceGmIndex = lastGmLogIndex(source);
      JSONObject gm = source.getJSONArray("log").getJSONObject(sourceGmIndex);
      sourceGmText = gm.getString("text");
      if (sourceTurn != current.optInt("turn", -1) || sourceGmIndex != lastGmLogIndex(current)
          || !sourceGmText.equals(current.getJSONArray("log")
              .getJSONObject(sourceGmIndex).optString("text", ""))) return;
    } catch (Exception error) { return; }
    final String key = baseHash + "|" + sourceTurn + "|" + sourceGmIndex + "|"
        + JSONObject.quote(sourceGmText) + "|" + JSONObject.quote(choicesJson);
    if (key.equals(activePrefetchKey)) return;
    final long generation = prefetchGeneration.incrementAndGet();
    prefetchCache = null;
    activePrefetchKey = key;
    try { prefetchIo.execute(() -> {
      try {
        JSONArray choices = new JSONArray(choicesJson);
        if (choices.length() != 3) return;
        if (generation != prefetchGeneration.get() || !baseHash.equals(gameCore.currentStateHash())) return;
        Map<String, String> actions = new LinkedHashMap<>();
        Map<String, JSONObject> previews = new LinkedHashMap<>();
        StringBuilder prompt = new StringBuilder(
            "Generate exactly one independent next-turn narration per branch A/B/C. "
                + "Each branch has its own hypothetical Core-committed outcome and canon. "
                + "Never transfer events, facts, entities, loot or future choices between branches. "
                + "Each reply must be at most 1800 characters, choices 0-3. "
                + "Return only JSON with branches A, B and C; each contains reply, choices and encounterDialogue.\n");
        for (int i = 0; i < 3; i++) {
          String id = String.valueOf((char) ('A' + i));
          JSONObject choice = choices.getJSONObject(i);
          String action = choice.optString("action", "").trim();
          if (!id.equals(choice.optString("id", "")) || action.isEmpty()
              || actions.containsValue(action)) return;
          JSONObject preview = new JSONObject(gameCore.previewTurn(action, baseHash));
          if (!preview.optBoolean("handled", false)) return;
          actions.put(id, action);
          previews.put(id, preview);
          prompt.append("\n=== BRANCH ").append(id).append(" ONLY ===\n")
              .append(narrationPrompt(preview.getJSONObject("state"), action)).append('\n');
        }
        if (generation != prefetchGeneration.get() || !baseHash.equals(gameCore.currentStateHash())) return;
        Map<String, PrefetchBranch> valid = new LinkedHashMap<>();
        Map<String, JSONObject> generatedBranches = GmBranchBatch.generate(
            prompt.toString(), actions, previews, this::geminiBranchBatch);
        for (Map.Entry<String, JSONObject> entry : generatedBranches.entrySet()) {
          String id = entry.getKey();
          JSONObject preview = previews.get(id);
          valid.put(id, new PrefetchBranch(actions.get(id), preview.getString("outcomeHash"), entry.getValue()));
        }
        synchronized (MainActivity.this) {
          if (!destroyed && generation == prefetchGeneration.get()
              && baseHash.equals(gameCore.currentStateHash())) {
            prefetchCache = new PrefetchCache(baseHash, sourceTurn, sourceGmIndex, sourceGmText, valid);
          }
        }
      } catch (Exception error) {
        Log.w(TAG, "Branch prefetch unavailable; normal turn path remains available: "
            + providerErrorSummary(error));
      }
    }); } catch (java.util.concurrent.RejectedExecutionException ignored) {}
  }

  static JSONObject narrationFallback(JSONObject state, String replyHint) {
    return narrationFallback(state, replyHint, "");
  }

  static JSONObject narrationFallback(JSONObject state, String replyHint, String action) {
    JSONObject generated = new JSONObject();
    try {
      String reply = replyHint == null ? "" : replyHint.trim();
      if (reply.startsWith("Rương chứa ")) {
        String contents = reply.substring("Rương chứa ".length())
            .replace("Đã thêm vào Inventory.", "").trim();
        int reward = contents.indexOf("Nhận +");
        if (reward >= 0) contents = contents.substring(0, reward).trim();
        reply = "Cao Minh mở nắp rương. Bên trong hiện ra " + contents;
      }
      JSONObject route = state == null ? null : state.optJSONObject("levelRoute");
      String result = route != null && state != null
          && route.optInt("lastRollTurn", -1) == state.optInt("turn", 1)
          ? route.optString("lastResult", "") : "";
      String location = state == null ? "khu vực hiện tại"
          : state.optString("location", "khu vực hiện tại");
      String levelKey = state == null ? "" : state.optString("currentLevelKey", "");
      boolean levelZero = "0".equals(levelKey) || location.startsWith("Level 0")
          || location.toLowerCase(java.util.Locale.ROOT).contains("hành lang vàng");
      if ("SUCCESS".equals(result)) {
        reply = "Qua những đoạn đường nối tiếp nhau, Cao Minh tiến sâu hơn trong " + location
            + ". Anh vẫn chưa nhận ra dấu hiệu nào cho thấy lối thoát ở gần.";
      } else if ("RESET".equals(result)) {
        reply = "Sau một khúc ngoặt, Cao Minh lại đối diện cảnh vật quen thuộc ở "
            + location + ". Con đường vừa đi đã vòng trở lại điểm cũ.";
      } else if ("EXIT_AVAILABLE".equals(result)) {
        reply = "Giữa " + location
            + ", Cao Minh nhận ra một lối ra có thể dẫn sang chặng tiếp theo."
            + " Phía bên kia lối ra ấy vẫn còn chờ được khám phá.";
      }
      if (reply.isEmpty()) {
        String submittedAction = action == null ? "" : action.trim().toLowerCase(java.util.Locale.ROOT);
        if (levelZero && submittedAction.contains("sau trận chiến")) {
          reply = "Sau trận chiến, Cao Minh đưa mắt rà soát những bức tường vàng nhạt."
              + " Dãy hành lang vẫn trải dài dưới ánh đèn huỳnh quang;"
              + " lúc này, anh chưa nhận ra dấu hiệu nào đủ rõ để chọn hướng đi.";
        } else if (levelZero && (submittedAction.contains("lắng nghe")
            || submittedAction.contains("nghe "))) {
          reply = "Cao Minh dừng lại lắng nghe giữa dãy hành lang vàng nhạt."
              + " Tiếng đèn huỳnh quang đều đều phía trên hòa vào khoảng không;"
              + " chưa có âm thanh nào giúp anh xác định điều gì chờ ở đoạn tiếp theo.";
        } else if (levelZero) {
          reply = "Cao Minh nhìn dọc những bức tường vàng nhạt."
              + " Ánh đèn huỳnh quang phủ lên đoạn hành lang trước mắt;"
              + " những gì nhìn thấy vẫn chưa đủ để anh biết lối nào dẫn ra ngoài.";
        } else {
          reply = "Trước mắt Cao Minh là " + location
              + ". Quang cảnh nơi đây vẫn chưa đem đến manh mối rõ ràng"
              + " về hướng anh có thể tìm hiểu tiếp.";
        }
      }
      JSONObject emergent = state == null ? null : state.optJSONObject("emergent");
      JSONObject selection = emergent == null ? null : emergent.optJSONObject("lastSelection");
      if (selection != null && !selection.optBoolean("selectedNone", false)) {
        String committedSummary = selection.optString("publicSummary", "").trim();
        if (!committedSummary.isEmpty() && !reply.contains(committedSummary)) {
          reply += "\n\n" + committedSummary;
        }
      }
      JSONArray fallbackDialogue = new JSONArray();
      JSONObject encounter = state == null ? null : state.optJSONObject("characterEncounter");
      JSONArray pending = encounter == null ? null : encounter.optJSONArray("pendingIntro");
      if (pending != null && pending.length() > 0) {
        String id = pending.optString(0, "");
        String name = "lucia".equals(id) ? "Lucia Lục"
            : "luc_tram".equals(id) ? "Lục Trầm"
            : "syvial".equals(id) ? "Syvial" : "Người đồng hành";
        fallbackDialogue.put(name + " cất tiếng khi Cao Minh đến gần.");
        fallbackDialogue.put("Cả hai trao đổi vài lời rồi tiếp tục quan sát khu vực.");
      }
      JSONObject flags = state == null ? null : state.optJSONObject("flags");
      JSONObject combat = state == null ? null : state.optJSONObject("combat");
      boolean activeEncounter = flags != null
          && !flags.optString("entityEncounterKey", "").trim().isEmpty();
      boolean deathPending = combat != null && combat.optBoolean("deathRestartPending", false);
      JSONArray suggestions = new JSONArray();
      if (!activeEncounter && !deathPending) {
        boolean exitOpen = route != null && route.optBoolean("exitAvailable", false);
        if (exitOpen) {
          suggestions.put(new JSONObject().put("text", "Đi qua lối ra đến chặng kế tiếp"));
          suggestions.put(new JSONObject().put("text", "Thận trọng tiếp cận lối thoát vừa tìm thấy"));
        } else {
          suggestions.put(new JSONObject().put("text", "Tiếp tục khám phá khu vực phía trước"));
          suggestions.put(new JSONObject().put("text", "Khảo sát những âm thanh khác thường gần đó"));
        }
      }
      generated.put("reply", reply)
          .put("choices", suggestions)
          .put("encounterDialogue", fallbackDialogue);
    } catch (Exception ignored) {}
    return generated;
  }

  private void emit(String function, String json) {
    String script = "window." + function + "(" + JSONObject.quote(json) + ")";
    runOnUiThread(() -> { if (!destroyed && webView != null) webView.evaluateJavascript(script, null); });
  }

  private class GameBridge {
    @JavascriptInterface public void prefetchNarrativeChain(String sourceJson, String choicesJson) {
      MainActivity.this.prefetchNarrativeChain(sourceJson, choicesJson);
    }

    @JavascriptInterface public void prefetchChoices(String sourceJson, String choicesJson) {
      MainActivity.this.prefetchChoices(sourceJson, choicesJson);
    }

    @JavascriptInterface public String saveCheckpoint() {
      return gameCore.saveCheckpoint();
    }

    @JavascriptInterface public String loadCheckpoint() {
      invalidatePrefetch();
      return gameCore.loadCheckpoint();
    }

    @JavascriptInterface public void clearCheckpoint() {
      invalidatePrefetch();
      gameCore.clearCheckpoint();
    }

    @JavascriptInterface public void submitTurn(String stateJson, String action) {
      submitTurnInternal(stateJson, action, null, -1, -1, "");
    }

    @JavascriptInterface public void submitChoice(String stateJson, String action, String choiceId,
                                                  int sourceTurn, int sourceGmIndex, String sourceGmText) {
      submitTurnInternal(stateJson, action, choiceId, sourceTurn, sourceGmIndex, sourceGmText);
    }

    private void submitTurnInternal(String stateJson, String action, String choiceId,
                                    int sourceTurn, int sourceGmIndex, String sourceGmText) {
      if (destroyed || !turnInFlight.compareAndSet(false, true)) return;
      final PrefetchCache ready = prefetchCache;
      invalidatePrefetch();
      try { io.execute(() -> {
        JSONObject committedBeforeNarration = null;
        long tStart = System.currentTimeMillis();
        try {
          JSONObject submitted = new JSONObject(stateJson);
          JSONObject persisted = new JSONObject(gameCore.currentCoreState());
          if (persisted.length() > 0) submitted = persisted;
          if (choiceId != null && (sourceTurn != submitted.optInt("turn", -1)
              || sourceGmIndex != lastGmLogIndex(submitted)
              || !sourceGmText.equals(submitted.getJSONArray("log")
                  .getJSONObject(sourceGmIndex).optString("text", "")))) {
            throw new Exception("Lựa chọn đã thuộc lượt cũ. Hãy chọn trên lượt hiện tại.");
          }
          String baseHash = gameCore.currentStateHash();
          PrefetchBranch cached = ready != null && ready.baseHash.equals(baseHash)
              && choiceId != null ? ready.forChoice(choiceId, action == null ? "" : action.trim(),
                  sourceTurn, sourceGmIndex, sourceGmText) : null;
          String actualAction = action == null ? "" : action.trim();
          boolean mechanical = gameCore.isMechanicalAction(actualAction);
          JSONArray visibleChoices = initialNarrativeChoices(submitted);
          if (choiceId != null && visibleChoices.length() == 2) {
            int slot = "A".equals(choiceId) ? 0 : "B".equals(choiceId) ? 1 : -1;
            if (slot < 0 || !actualAction.equals(
                visibleChoices.getJSONObject(slot).optString("action",
                    visibleChoices.getJSONObject(slot).getString("text")))) {
              throw new Exception("Lựa chọn không khớp với nội dung đã hiển thị.");
            }
          }
          HiddenNarrativeChain chain = hiddenChain;
          HiddenNarrativeChain.Beat beat = !mechanical && chain != null
              ? chain.current(gameCore.currentNarrativeWorldHash(), visibleChoices) : null;
          if (chain != null && beat == null) hiddenChain = null;
          String canonicalAction = actualAction;
          if (!mechanical) {
            if (beat != null) canonicalAction = beat.canonicalAction;
            else if (visibleChoices.length() == 2) {
              canonicalAction = visibleChoices.getJSONObject(0).optString("action",
                  visibleChoices.getJSONObject(0).getString("text"));
            }
          }

          if (CombatChoiceEngine.isActive(submitted)) {
            throw new Exception("Đang chiến đấu. Hãy dùng khung Poker Dice trong GAME MASTER.");
          }

          String existingEncounter = encounterKey(submitted);
          if (CombatChoiceEngine.isKnownEntity(existingEncounter)) {
            submitted = new JSONObject(
                gameCore.startCombatRuntime(existingEncounter, lastGmLogIndex(submitted)));
            emit("backroomCombatDiceState", submitted.toString());
            return;
          }

          JSONObject prepared = new JSONObject(
              gameCore.processRule(submitted.toString(), canonicalAction));
          if (prepared.optBoolean("handled", false)) {
            emit("backroomTurn", prepared.getJSONObject("state").toString());
            return;
          }
          if (!"turn_prepared".equals(prepared.optString("reason", ""))) {
            throw new Exception(prepared.optString("error", "Game State Core không thể chuẩn bị lượt."));
          }

          String turnId = prepared.getString("turnId");
          JSONObject selected = prepared.optJSONObject("selectedCandidate");
          // Core's canonical tactic is also used by previewTurn; the action needs one narration call.
          JSONObject committed = new JSONObject(
              gameCore.completePreparedTurn(turnId, "{}"));
          if (!committed.optBoolean("handled", false)) {
            throw new Exception(committed.optString("error", "Game State Core từ chối COMMIT."));
          }

          JSONObject state = committed.getJSONObject("state");
          committedBeforeNarration = new JSONObject(state.toString());
          String replyHint = committed.optString("replyHint", "");

          JSONObject generated;
          String reply;
          boolean narrationValidated = false;
          final HiddenNarrativeChain.Beat selectedBeat = beat;
          final boolean forecastMatches = selectedBeat != null
              && selectedBeat.afterWorldHash.equals(gameCore.currentNarrativeWorldHash());
          if (selectedBeat != null && !forecastMatches) hiddenChain = null;
          final boolean directCached = forecastMatches
              && ("A".equals(choiceId) || "B".equals(choiceId));
          try {
            final JSONObject narrationState = state;
            final String gmAction = actualAction;
            final String coreAction = canonicalAction;
            generated = awaitNarration(narrationIo, () -> {
              boolean legacyHit = cached != null && cached.outcomeHash.equals(gameCore.currentStateHash());
              JSONObject draft = directCached ? selectedBeat.narration(choiceId)
                  : legacyHit ? new JSONObject(cached.narration.toString()) : null;
              String prompt = narrationPrompt(narrationState, coreAction);
              if (!coreAction.equals(gmAction)) {
                prompt += "\nPLAYER'S ACTUAL ACTION: " + gmAction
                    + "\nHIDDEN CHAIN: The fixed Core outcome has already happened."
                    + " Tell how the player's ACTUAL action leads organically to this exact"
                    + " outcome; do not narrate the hidden canonical action as a player choice."
                    + " Never reveal that both choices or free action share the same outcome.";
              }
              if (forecastMatches && choiceId == null) {
                prompt += "\nCANONICAL STORY DESTINATION (reference ONLY, not a"
                    + " mandatory route): " + selectedBeat.replyA
                    + "\nRebuild a genuinely different, causal scene grounded in the"
                    + " player's actual action. Never contradict Core state or invent events.";
              }
              if (draft == null) {
                if (BuildConfig.DEBUG) Log.d(TAG, forecastMatches && choiceId == null
                    ? "HIDDEN_CHAIN_PLAYER_ACTION_REWRITE" : "HIDDEN_CHAIN_UNCACHED_NARRATION");
                draft = parseModelJson(generateText(prompt));
              } else if (BuildConfig.DEBUG && directCached) {
                Log.d(TAG, "HIDDEN_CHAIN_CACHE_HIT choice=" + choiceId);
              }
              if (forecastMatches) draft.put("choices",
                  new JSONArray(selectedBeat.nextChoices.toString()));
              final String retryPrompt = prompt;
              return NarrationGuard.regenerateIfInvalid(draft, narrationState, gmAction,
                  violation -> {
                    JSONObject retry = parseModelJson(generateText(retryPrompt
                        + "\nVALIDATION REJECTED: " + violation
                        + "\nRewrite only the narrative; never change world outcomes."
                        + " Return valid JSON."));
                    if (forecastMatches) retry.put("choices",
                        new JSONArray(selectedBeat.nextChoices.toString()));
                    return retry;
                  });
            }, TimeUnit.SECONDS.toMillis(NARRATION_DEADLINE_SECONDS));
            reply = generated.optString("reply", "").trim();
            narrationValidated = true;
          } catch (Exception narrationError) {
            Log.w(TAG, "Narration failed or exceeded its deadline; using deterministic template: "
                + providerErrorSummary(narrationError));
            generated = narrationFallback(state, replyHint, action);
            reply = generated.optString("reply", "");
            narrationValidated = NarrationGuard.validate(generated, state, action).isEmpty();
          }

          JSONArray encounterDialogue = generated.optJSONArray("encounterDialogue");
          if (encounterDialogue == null) encounterDialogue = new JSONArray();
          reply = appendEncounterDialogue(reply, encounterDialogue);

          JSONArray log = state.optJSONArray("log");
          if (log == null) log = new JSONArray();
          log.put(new JSONObject().put("role", "player").put("text", action));
          JSONObject gmEntry = GmChoiceContract.gmEntry(reply, generated, state);
          String newEncounter = encounterKey(state);
          if (CombatChoiceEngine.isKnownEntity(newEncounter)) gmEntry.remove("choices");
          log.put(gmEntry);
          state.put("log", log);

          boolean acknowledgePendingIntro = narrationValidated
              && encounterDialogue.length() >= 2 && encounterDialogue.length() <= 5;
          state = new JSONObject(
              gameCore.commitNarration(state.toString(), acknowledgePendingIntro, narrationValidated));

          // Advance only when the predicted Core state and next two choices agree.
          if (selectedBeat != null && forecastMatches && narrationValidated
              && generated.optJSONArray("choices") != null
              && GmChoiceContract.sanitizeChoices(generated.getJSONArray("choices")).toString()
                  .equals(selectedBeat.nextChoices.toString())) {
            if (hiddenChain == chain && !chain.consume(selectedBeat)) hiddenChain = null;
          } else if (hiddenChain == chain) {
            hiddenChain = null;
          }

          if (CombatChoiceEngine.isKnownEntity(newEncounter)) {
            state = new JSONObject(
                gameCore.startCombatRuntime(newEncounter, log.length() - 1));
          }

          if (BuildConfig.DEBUG) {
            Log.d(TAG, "EMERGENT TURN TELEMETRY: total=" + (System.currentTimeMillis() - tStart)
                + "ms turnId=" + turnId
                + " situation=" + (selected == null ? "NONE" : selected.optString("situationKey", "NONE")));
          }
          emit("backroomTurn", state.toString());
        } catch (Exception e) {
          String message = e.getMessage() == null ? "Không thể xử lý lượt." : e.getMessage();
          if (committedBeforeNarration != null) {
            try {
              JSONObject payload = new JSONObject()
                  .put("state", committedBeforeNarration)
                  .put("message", message);
              emit("backroomCommittedError", payload.toString());
            } catch (Exception ignored) {
              emit("backroomError", message);
            }
          } else {
            emit("backroomError", message);
          }
        } finally {
          turnInFlight.set(false);
        }
      }); } catch (java.util.concurrent.RejectedExecutionException ignored) { turnInFlight.set(false); }
    }

    @JavascriptInterface public void combatRoll(String stateJson) {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject runtime = new JSONObject(gameCore.combatRollRuntime());
          emit("backroomCombatDiceState", runtime.toString());
        } catch (Exception e) {
          emit("backroomError", e.getMessage() == null ? "Không thể ROLL." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void combatHold(String stateJson, int dieIndex, boolean held) {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject runtime = new JSONObject(gameCore.combatHoldRuntime(dieIndex, held));
          emit("backroomCombatDiceState", runtime.toString());
        } catch (Exception e) {
          emit("backroomError", e.getMessage() == null ? "Không thể HOLD die." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void combatFinish(String stateJson) {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject runtime = new JSONObject(gameCore.combatFinishRuntime());
          emit("backroomCombatDiceState", runtime.toString());
        } catch (Exception e) {
          emit("backroomError", e.getMessage() == null ? "Không thể FINISH hand." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void combatResolve(String stateJson) {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject result = new JSONObject(gameCore.processCombatResolution(stateJson));
          if (!result.optBoolean("handled", false)) {
            throw new Exception(result.optString("error", "Không thể resolve combat hand."));
          }
          emit("backroomCombatTurn", result.getJSONObject("state").toString());
        } catch (Exception e) {
          emit("backroomError", e.getMessage() == null ? "Không thể resolve combat hand." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void restartAfterDeath() {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject result = new JSONObject(gameCore.restartAfterDeath());
          if (!result.optBoolean("handled", false)) {
            throw new Exception(result.optString("error", "Không thể bắt đầu lại từ đầu Level."));
          }
          emit("backroomTurn", result.getJSONObject("state").toString());
        } catch (Exception e) {
          emit("backroomError",
              e.getMessage() == null ? "Không thể bắt đầu lại từ đầu Level." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void coreUpgrade(String stateJson, String characterId, String stat) {
      invalidatePrefetch();
      io.execute(() -> emit("backroomCoreUpgrade",
          gameCore.processCoreUpgrade(stateJson, characterId, stat)));
    }

    @JavascriptInterface public void itemAction(String stateJson, String ownerId, String itemId,
                                                String operation, String targetId, int quantity) {
      invalidatePrefetch();
      io.execute(() -> {
        try {
          JSONObject submitted = new JSONObject(gameCore.currentCoreState());
          if (CombatChoiceEngine.isActive(submitted)) {
            JSONObject rejected = new JSONObject()
              .put("handled", false)
              .put("state", submitted)
              .put("reason", "combat_locked")
              .put("error", "Battle đang hoạt động. Hãy hoàn tất Poker Dice trước.");
            emit("backroomItemAction", rejected.toString());
            return;
          }
          emit("backroomItemAction",
              gameCore.processItemAction(stateJson, ownerId, itemId, operation, targetId, quantity));
        } catch (Exception e) {
          JSONObject rejected = new JSONObject();
          try {
            rejected.put("handled", false).put("state", new JSONObject(stateJson));
            rejected.put("error", e.getMessage() == null ? "Không thể xử lý vật phẩm." : e.getMessage());
          } catch (Exception ignored) {}
          emit("backroomItemAction", rejected.toString());
        }
      });
    }

    @JavascriptInterface public String levelSnapshot(String stateJson) {
      return gameCore.levelSnapshotDescriptor(stateJson);
    }

    @JavascriptInterface public String normalizeState(String stateJson) {
      invalidatePrefetch();
      return gameCore.normalizeState(stateJson);
    }

    @JavascriptInterface public String startNewGame(String initialJson) {
      invalidatePrefetch();
      return gameCore.startNewGame(initialJson);
    }
  }

  private static class HttpError extends Exception {
    final int status;
    HttpError(int status, String message) { super(message); this.status = status; }
  }
}
