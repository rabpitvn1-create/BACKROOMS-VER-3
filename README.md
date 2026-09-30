# BACKROOMS Android Text Game

Android/WebView text game. V3 builds directly from checked-in source; there is no runtime patch chain.

## Runtime architecture

- `android-apk/app/src/main/assets/index.html` and the adjacent JS/CSS assets implement the WebView UI.
- `android-apk/app/src/main/java/com/rabpit/backroom/MainActivity.java` owns the Android bridge and GM-provider orchestration.
- `android-apk/app/src/main/java/com/rabpit/backroom/core/` is Java 17 Core code for authoritative state transitions, save migration, levels, character/entity encounters, items, Poker Dice combat, beliefs/evidence and narration contracts.
- Core commits outcomes first. AI narration receives a read-only packet and cannot decide damage, proc, spawn, loot, Party membership, Level transitions or other authoritative outcomes.
- `canon/` is the editable repository canon source. `tools/canon.py` validates stable IDs/provenance and generates the runtime canon/knowledge projections under `android-apk/app/src/main/assets/`.
- `CanonRetriever` loads the generated `assets/canon/canon_index.json`, selects stable canon IDs for the committed scene, resolves required references and applies knowledge visibility gates before material reaches the narration prompt. `EpistemicView` and the existing belief/evidence state remain the dynamic epistemic source.
- `LevelCore`, `EntityCore` and related Core components consume generated JSON projections from the same root canon source; generated assets are not editing authorities.

The detailed authoring, conflict and migration rules are in [`canon/README.md`](canon/README.md). Resolved and unresolved source conflicts are recorded in [`canon/CONFLICTS.md`](canon/CONFLICTS.md).

## Validate and build

From repository root:

```bash
python3 tools/canon.py check
node android-apk/app/src/test/js/gm-choice-ui.test.cjs
cd android-apk
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

After editing canon source, regenerate before running the checks:

```bash
python3 tools/canon.py generate
python3 tools/canon.py check
```

The debug APK is written to `android-apk/app/build/outputs/apk/debug/app-debug.apk`.

CI uses Java 17 and Gradle 8.10.2 in `.github/workflows/v3-emergent-validation.yml`; the release workflow is `.github/workflows/build-backroom-apk.yml`.
