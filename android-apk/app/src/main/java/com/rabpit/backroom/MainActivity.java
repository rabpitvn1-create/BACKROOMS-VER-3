package com.rabpit.backroom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.rabpit.backroom.core.GameCoreFacade;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
  private WebView webView;
  private final ExecutorService io = Executors.newSingleThreadExecutor();
  private GameCoreFacade gameCore;
  private static final String GEMINI_MODEL = "gemini-3.6-flash";
  private static final int[] RETRYABLE = {408, 429, 500, 502, 503, 504};

  @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    gameCore = GameCoreFacade.create(getApplicationContext(), BuildConfig.DEBUG);
    webView = new WebView(this);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(true);
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        installUiEnhancements();
      }
    });
    webView.addJavascriptInterface(new GameBridge(), "Android");
    setContentView(webView);
    webView.loadUrl("file:///android_asset/index.html");
  }

  @Override protected void onDestroy() {
    if (gameCore != null) gameCore.close();
    io.shutdownNow();
    if (webView != null) webView.destroy();
    super.onDestroy();
  }

  private void installUiEnhancements() {
    String script =
      "(function(){" +
      "if(window.__backroomEnhancements)return;window.__backroomEnhancements=true;" +
      "var st=document.createElement('style');" +
      "st.textContent='button{transition:transform 80ms ease,background 120ms ease,border-color 120ms ease;touch-action:manipulation;-webkit-tap-highlight-color:rgba(255,255,255,.12)}button:active:not(:disabled){transform:scale(.965);background:#303840;border-color:#77828c}button:disabled{opacity:.48;cursor:not-allowed}.snapshot-placeholder{display:grid;place-items:center;gap:7px;text-align:center;color:#69737c}.snapshot-placeholder b{font-size:12px;letter-spacing:.16em}.snapshot-placeholder small{color:#56616a}.message.pending{opacity:.72}.message.pending .text{color:#aeb7be}';" +
      "document.head.appendChild(st);" +
      "function scrollBottom(){var l=document.getElementById('log');if(l)requestAnimationFrame(function(){l.scrollTop=l.scrollHeight;});}" +
      "function renderSnapshot(){var box=document.getElementById('snapshot');if(!box)return;box.textContent='';var r=null;try{if(window.Android&&typeof Android.levelSnapshot==='function')r=JSON.parse(Android.levelSnapshot(Number(state&&state.turn)||1));}catch(e){}if(r&&r.path){var img=document.createElement('img');img.src=r.path;img.alt='Level '+r.level+' Snapshot';box.appendChild(img);}else{var p=document.createElement('div');p.className='snapshot-placeholder';p.innerHTML='<b>LEVEL SNAPSHOT</b><small>Không có ảnh local cho Level hiện tại.</small>';box.appendChild(p);}}" +
      "var oldRender=window.render;if(typeof oldRender==='function'){window.render=function(){oldRender();renderSnapshot();scrollBottom();};}" +
      "var oldTurn=window.backroomTurn;window.backroomTurn=function(json){if(typeof oldTurn==='function')oldTurn(json);document.querySelectorAll('[data-pending=\"1\"]').forEach(function(n){n.remove();});renderSnapshot();scrollBottom();};" +
      "var oldError=window.backroomError;window.backroomError=function(message){document.querySelectorAll('[data-pending=\"1\"]').forEach(function(n){n.remove();});if(typeof oldError==='function')oldError(message);scrollBottom();};" +
      "var f=document.getElementById('form');if(f){f.addEventListener('submit',function(){var a=document.getElementById('action');var text=a?a.value.trim():'';if(!text)return;var l=document.getElementById('log');if(!l)return;var player=document.createElement('article');player.className='message player pending';player.setAttribute('data-pending','1');player.innerHTML='<div class=\"role\">BẠN</div><div class=\"text\"></div>';player.querySelector('.text').textContent=text;l.appendChild(player);var gm=document.createElement('article');gm.className='message pending';gm.setAttribute('data-pending','1');gm.innerHTML='<div class=\"role\">GAME MASTER</div><div class=\"text\">Đang xử lý lượt…</div>';l.appendChild(gm);scrollBottom();},true);}" +
      "try{localStorage.removeItem('backroom-apk-snapshot');}catch(e){}renderSnapshot();scrollBottom();" +
      "})();";
    webView.evaluateJavascript(script, null);
  }

  private boolean retryable(int code) {
    for (int value : RETRYABLE) if (value == code) return true;
    return false;
  }

  private String[] geminiKeys() {
    return new String[] {
      BuildConfig.GEMINI_API_KEY_1,
      BuildConfig.GEMINI_API_KEY_2,
      BuildConfig.GEMINI_API_KEY_3
    };
  }

  private String postJson(String endpoint, String key, String authHeader, JSONObject payload) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
    connection.setRequestMethod("POST");
    connection.setConnectTimeout(20000);
    connection.setReadTimeout(60000);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty(authHeader, authHeader.equals("Authorization") ? "Bearer " + key : key);
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

  private String geminiText(String prompt) throws Exception {
    Exception last = null;
    for (String key : geminiKeys()) {
      if (key == null || key.isEmpty()) continue;
      for (int attempt = 0; attempt < 2; attempt++) {
        try {
          JSONObject part = new JSONObject().put("text", prompt);
          JSONObject contents = new JSONObject().put("role", "user").put("parts", new JSONArray().put(part));
          JSONObject config = new JSONObject().put("responseMimeType", "application/json").put("temperature", 0.8);
          JSONObject body = new JSONObject().put("contents", new JSONArray().put(contents)).put("generationConfig", config);
          JSONObject result = new JSONObject(postJson("https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent", key, "x-goog-api-key", body));
          JSONArray candidates = result.optJSONArray("candidates");
          StringBuilder text = new StringBuilder();
          if (candidates != null) {
            for (int c = 0; c < candidates.length(); c++) {
              JSONObject candidate = candidates.optJSONObject(c);
              JSONObject providerContent = candidate != null ? candidate.optJSONObject("content") : null;
              JSONArray parts = providerContent != null ? providerContent.optJSONArray("parts") : null;
              if (parts == null) continue;
              for (int p = 0; p < parts.length(); p++) {
                JSONObject responsePart = parts.optJSONObject(p);
                String piece = responsePart != null ? responsePart.optString("text", "").trim() : "";
                if (!piece.isEmpty()) {
                  if (text.length() > 0) text.append('\n');
                  text.append(piece);
                }
              }
            }
          }
          if (text.length() == 0) throw new Exception("Gemini không trả nội dung.");
          return text.toString();
        } catch (Exception e) {
          last = e;
          int code = e instanceof HttpError ? ((HttpError)e).status : 0;
          if (attempt == 0 && (code == 0 || retryable(code))) {
            try { Thread.sleep(350); } catch (InterruptedException ignored) {}
            continue;
          }
          break;
        }
      }
    }
    throw last != null ? last : new Exception("Không có Gemini API key trong APK.");
  }

  private boolean haikuConfigured() {
    return BuildConfig.HAIKU_API_KEY != null && !BuildConfig.HAIKU_API_KEY.trim().isEmpty()
        && BuildConfig.HAIKU_MODEL != null && !BuildConfig.HAIKU_MODEL.trim().isEmpty()
        && BuildConfig.HAIKU_BASE_URL != null && !BuildConfig.HAIKU_BASE_URL.trim().isEmpty();
  }

  private String haikuModel() throws Exception {
    String model = BuildConfig.HAIKU_MODEL == null ? "" : BuildConfig.HAIKU_MODEL.trim();
    if (model.isEmpty()) throw new Exception("HAIKU_MODEL chưa được cấu hình.");
    return model;
  }

  private String haikuBaseUrl() throws Exception {
    String base = BuildConfig.HAIKU_BASE_URL == null ? "" : BuildConfig.HAIKU_BASE_URL.trim();
    if (base.isEmpty()) throw new Exception("HAIKU_BASE_URL chưa được cấu hình.");
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
    connection.setConnectTimeout(20000);
    connection.setReadTimeout(60000);
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    if (anthropic) {
      connection.setRequestProperty("x-api-key", BuildConfig.HAIKU_API_KEY);
      connection.setRequestProperty("anthropic-version", "2023-06-01");
    } else {
      connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.HAIKU_API_KEY);
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
    return body.toString();
  }

  private String haikuAnthropicText(String prompt) throws Exception {
    JSONObject body = new JSONObject()
        .put("model", haikuModel())
        .put("max_tokens", 2048)
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

  private String haikuOpenAiText(String prompt) throws Exception {
    JSONObject body = new JSONObject()
        .put("model", haikuModel())
        .put("temperature", 0.6)
        .put("max_tokens", 2048)
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
      text.append(((String) rawContent).trim());
    } else if (rawContent instanceof JSONArray) {
      JSONArray parts = (JSONArray) rawContent;
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

  private boolean haikuProtocolMismatch(Exception error) {
    if (!(error instanceof HttpError)) return false;
    int status = ((HttpError) error).status;
    return status == 400 || status == 404 || status == 405 || status == 415 || status == 422;
  }

  private String haikuTextOnce(String prompt) throws Exception {
    String base = haikuBaseUrl();
    String output;
    if (base.endsWith("/chat/completions")) {
      output = haikuOpenAiText(prompt);
    } else if (base.endsWith("/messages") || base.contains("api.anthropic.com")) {
      output = haikuAnthropicText(prompt);
    } else {
      try {
        output = haikuOpenAiText(prompt);
      } catch (Exception openAiError) {
        if (!haikuProtocolMismatch(openAiError)) throw openAiError;
        output = haikuAnthropicText(prompt);
      }
    }
    parseModelJson(output);
    return output;
  }

  private String haikuText(String prompt) throws Exception {
    if (!haikuConfigured()) throw new Exception("Haiku fallback chưa được cấu hình đầy đủ.");
    Exception last = null;
    for (int attempt = 0; attempt < 2; attempt++) {
      try {
        return haikuTextOnce(prompt);
      } catch (Exception error) {
        last = error;
        int status = error instanceof HttpError ? ((HttpError) error).status : 0;
        boolean retry = attempt == 0 && (status == 0 || status == 408 || status == 500
            || status == 502 || status == 503 || status == 504);
        if (!retry) break;
        try {
          Thread.sleep(800);
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    throw last != null ? last : new Exception("Haiku không khả dụng.");
  }

  private String providerErrorSummary(Exception error) {
    if (error == null) return "không xác định";
    String message = error.getMessage();
    if (message == null || message.trim().isEmpty()) return error.getClass().getSimpleName();
    return message.length() > 220 ? message.substring(0, 220) : message;
  }

  private String generateText(String prompt) throws Exception {
    Exception geminiError;
    try {
      return geminiText(prompt);
    } catch (Exception error) {
      geminiError = error;
    }

    try {
      return haikuText(prompt);
    } catch (Exception haikuError) {
      throw new Exception("5 Gemini key và Haiku fallback đều không khả dụng. Gemini: "
          + providerErrorSummary(geminiError) + " | Haiku: " + providerErrorSummary(haikuError));
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

  private void mergeObject(JSONObject target, JSONObject patch) throws Exception {
    Iterator<String> keys = patch.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      target.put(key, patch.get(key));
    }
  }

  private void emit(String function, String json) {
    String script = "window." + function + "(" + JSONObject.quote(json) + ")";
    runOnUiThread(() -> webView.evaluateJavascript(script, null));
  }

  private class GameBridge {
    @JavascriptInterface public void submitTurn(String stateJson, String action) {
      io.execute(() -> {
        try {
          JSONObject localResult = new JSONObject(gameCore.processRule(stateJson, action));
          if (localResult.optBoolean("handled", false)) {
            emit("backroomTurn", localResult.getJSONObject("state").toString());
            return;
          }
          JSONObject state = new JSONObject(stateJson);
          String prompt = "Bạn là Game Master của text game Backrooms. Xử lý đúng một lượt và trả DUY NHẤT JSON hợp lệ, không markdown. " +
            "Viết tiếng Việt tự nhiên, đầy đủ ý. Không trả lời bằng câu rỗng. Không thay đổi dữ kiện chưa có căn cứ. Người chơi chỉ điều khiển nhân vật chính hiện tại. " +
            "State hiện tại: " + state.toString() + "\nHành động: " + action +
            "\nJSON bắt buộc: {\"reply\":\"phản hồi Game Master\",\"title\":\"giữ nguyên hoặc cập nhật\",\"location\":\"vị trí sau lượt\",\"player\":{},\"party\":[],\"inventory\":[],\"flags\":{}}";
          JSONObject generated = parseModelJson(generateText(prompt));
          String reply = generated.optString("reply", "").trim();
          if (reply.isEmpty()) throw new Exception("AI trả về phản hồi rỗng, lượt này không được ghi.");

          state.put("turn", state.optInt("turn", 1) + 1).put("mode", "ai");
          String title = generated.optString("title", "").trim();
          String location = generated.optString("location", "").trim();
          if (!title.isEmpty()) state.put("title", title);
          if (!location.isEmpty()) state.put("location", location);
          JSONObject coreCommit = new JSONObject(gameCore.processValidatedCandidate(stateJson, state.toString(), action));
          if (!coreCommit.optBoolean("handled", false)) {
            throw new Exception("Game State Core từ chối Gemini delta: " + coreCommit.optString("error", "invalid_delta"));
          }
          state = coreCommit.getJSONObject("state");

          JSONArray log = state.optJSONArray("log");
          if (log == null) log = new JSONArray();
          log.put(new JSONObject().put("role", "player").put("text", action));
          log.put(new JSONObject().put("role", "gm").put("text", reply));
          state.put("log", log);
          emit("backroomTurn", state.toString());
        } catch (Exception e) {
          emit("backroomError", e.getMessage() == null ? "Không thể xử lý lượt." : e.getMessage());
        }
      });
    }

    @JavascriptInterface public void coreUpgrade(String stateJson, String characterId, String stat) {
      io.execute(() -> emit("backroomCoreUpgrade", gameCore.processCoreUpgrade(stateJson, characterId, stat)));
    }

    @JavascriptInterface public void itemAction(String stateJson, String ownerId, String itemId,
                                                String operation, String targetId, int quantity) {
      io.execute(() -> emit("backroomItemAction",
          gameCore.processItemAction(stateJson, ownerId, itemId, operation, targetId, quantity)));
    }

    @JavascriptInterface public String combatState(String stateJson) {
      return gameCore.combatState(stateJson);
    }

    @JavascriptInterface public void combatHold(String stateJson, int dieIndex, boolean held) {
      io.execute(() -> emit("backroomCombat", gameCore.combatHold(stateJson, dieIndex, held)));
    }

    @JavascriptInterface public void combatRoll(String stateJson) {
      io.execute(() -> emit("backroomCombat", gameCore.combatRoll(stateJson)));
    }

    @JavascriptInterface public void combatFinish(String stateJson) {
      io.execute(() -> emit("backroomCombat", gameCore.combatFinish(stateJson)));
    }

    @JavascriptInterface public void combatResolve(String stateJson) {
      io.execute(() -> emit("backroomCombat", gameCore.combatResolve(stateJson)));
    }

    @JavascriptInterface public void exploreGameplay(String stateJson, String action) {
      io.execute(() -> emit("backroomGameplay", gameCore.processExplore(stateJson, action)));
    }

    @JavascriptInterface public void openChest(String stateJson) {
      io.execute(() -> emit("backroomGameplay", gameCore.openChest(stateJson)));
    }

    @JavascriptInterface public String levelSnapshot(int turn) {
      return gameCore.levelSnapshotDescriptor(turn);
    }
  }

  private static class HttpError extends Exception {
    final int status;
    HttpError(int status, String message) { super(message); this.status = status; }
  }
}
