from pathlib import Path
import re

MAIN = Path(__file__).resolve().parent / "app/src/main/java/com/rabpit/backroom/MainActivity.java"
text = MAIN.read_text(encoding="utf-8")

for required in [
    "private String haikuText(String prompt) throws Exception",
    "BuildConfig.HAIKU_API_KEY",
    "BuildConfig.HAIKU_MODEL",
    "BuildConfig.HAIKU_BASE_URL",
]:
    if required not in text:
        raise RuntimeError(f"Haiku fallback marker missing: {required}")

new_generate = r'''  private String generateText(String prompt) throws Exception {
    Exception geminiError;
    emit("backroomProvider", "Gemini");
    try {
      return geminiText(prompt);
    } catch (Exception error) {
      geminiError = error;
    }

    emit("backroomProvider", "Haiku");
    try {
      return haikuText(prompt);
    } catch (Exception haikuError) {
      throw new Exception("5 Gemini key và Haiku fallback đều không khả dụng. Gemini: "
          + providerErrorSummary(geminiError) + " | Haiku: " + providerErrorSummary(haikuError));
    }
  }
'''

pattern = r'  private String generateText\(String prompt\) throws Exception \{.*?\n  \}\n(?=\n  private JSONObject parseModelJson)'
text, count = re.subn(pattern, lambda _: new_generate.rstrip("\n"), text, count=1, flags=re.S)
if count != 1:
    raise RuntimeError(f"Haiku fallback generateText: expected 1 method, found {count}")

MAIN.write_text(text, encoding="utf-8")
print("Game Master provider order: five-key Gemini pool -> Haiku fallback.")
