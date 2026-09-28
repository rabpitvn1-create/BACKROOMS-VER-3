# BACKROOMS Android Text Game

Android/WebView game. V3 builds directly from checked-in source; there is no runtime patch chain.

## Runtime

- `android-apk/app/src/main/assets/index.html`: WebView UI.
- `android-apk/app/src/main/java/com/rabpit/backroom/MainActivity.java`: Android bridge + lightweight GM provider orchestration.
- `android-apk/app/src/main/java/com/rabpit/backroom/core/`: typed Kotlin state, save, inventory, party, physiology and combat adapters.
- `android-apk/app/src/main/java/com/rabpit/backroom/core/gameplay/`: gameplay rules rebuilt from V2: stats/progression, items/equipment, skills/entities, Poker Dice, level routing and deterministic RNG.
- `.github/workflows/build-backroom-apk.yml`: direct test/build/release pipeline.

Old Python patch scripts and character-specific Legacy systems are intentionally excluded from V3. Git history remains the recovery source if any old behavior is needed later.

## Build

```bash
cd android-apk
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

APK: `android-apk/app/build/outputs/apk/debug/app-debug.apk`.
