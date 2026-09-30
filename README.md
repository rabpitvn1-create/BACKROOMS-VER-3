# BACKROOMS Android Text Game

Android/WebView game. V3 builds directly from checked-in source; there is no runtime patch chain.

## Runtime

- `android-apk/app/src/main/assets/index.html`: WebView UI.
- `android-apk/app/src/main/java/com/rabpit/backroom/MainActivity.java`: Android bridge + lightweight GM provider orchestration.
- `android-apk/app/src/main/java/com/rabpit/backroom/core/`: Java game state, save, inventory, party, physiology, combat and narrative runtime.
- Android gameplay/runtime source under `android-apk/app/src/main/java/` is Java-only; the project no longer applies the Kotlin Android plugin.
- `.github/workflows/branch-cleanup.yml`: Agent Ponytail branch audit/cleanup; push runs audit-only, the weekly schedule prunes old branches already merged into `main`, and manual runs can opt into deletion.

Old Python patch scripts and character-specific Legacy systems are intentionally excluded from V3. Git history remains the recovery source if any old behavior is needed later.

## Build

```bash
cd android-apk
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

APK: `android-apk/app/build/outputs/apk/debug/app-debug.apk`.
