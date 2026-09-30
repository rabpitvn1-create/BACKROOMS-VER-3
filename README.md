# BACKROOMS Android Text Game

Android/WebView text game. V3 builds directly from checked-in source; there is no runtime patch chain.

## Runtime architecture

- `android-apk/app/src/main/assets/index.html` and adjacent JS/CSS assets implement the WebView UI.
- `android-apk/app/src/main/java/com/rabpit/backroom/MainActivity.java` owns the Android bridge, narrative prefetch/chain orchestration and GM-provider calls.
- `android-apk/app/src/main/java/com/rabpit/backroom/core/` is Java 17 Core code for authoritative state transitions, save migration, levels, character/entity encounters, items, Poker Dice combat, beliefs/evidence and narration contracts.
- Core commits gameplay outcomes first. AI narration receives read-only context and cannot decide damage, proc, spawn, loot, Party membership, Level transitions or other authoritative outcomes.
- `canon/` is the editable repository canon source. `tools/canon.py` validates stable IDs and provenance, then generates the runtime canon/knowledge projections under `android-apk/app/src/main/assets/`.
- `CanonRetriever` loads generated `assets/canon/canon_index.json`, resolves stable-ID dependencies for the committed scene and applies per-item knowledge gates before canon reaches narration.
- `EpistemicView` plus the existing belief/evidence state remain the dynamic actor-knowledge source.
- `LevelCore`, `EntityCore` and related Core components consume generated JSON projections from the same root canon source. Generated assets are not editing authorities.

Authoring, conflict and migration rules are documented in [`canon/README.md`](canon/README.md). Resolved and unresolved source conflicts are recorded in [`canon/CONFLICTS.md`](canon/CONFLICTS.md).

## Canon validation and build

From the repository root:

```bash
python3 tools/canon.py check
node android-apk/app/src/test/js/gm-choice-ui.test.cjs
cd android-apk
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

After editing canon source, regenerate projections before checking/building:

```bash
python3 tools/canon.py generate
python3 tools/canon.py check
```

The debug APK is written to `android-apk/app/build/outputs/apk/debug/app-debug.apk`. CI uses Java 17 and Gradle 8.10.2 in `.github/workflows/v3-emergent-validation.yml`; release builds use `.github/workflows/build-backroom-apk.yml`.

## Hidden narrative chain

The experiment keeps gameplay authority in Java Core and uses the remote narrator only for presentation.

- Core owns Level, route, Entity, combat, inventory, party and every committed world change.
- Normal exploration may prefetch a short hidden chain of ordinary beats. Each beat exposes two different approaches that may share the same Core outcome while the forecast remains fresh.
- Free-form Player Action is always submitted as the player's real action. If its Core outcome differs from the forecast, the prepared chain is discarded instead of forcing the player back onto it.
- Entity encounters, combat, chests and Level-transition mechanics interrupt or invalidate the chain.
- Narration is intentionally soft. Deterministic tests protect Core authority, cache freshness and interruption behavior; prose quality is evaluated by play rather than by a stochastic CI gate.
