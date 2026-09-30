# Hidden Narrative Chain — experimental verification

This protocol is for the feature branch only. Do **not** merge based on model-generated sample prose. Validate with the configured Gemini model and real gameplay.

## Automated gates

- PR workflow **Hidden Narrative Chain PR Tests** runs JS UI checks, Gradle JVM integration tests and builds an Android debug APK. The APK is uploaded as the run artifact named `hidden-narrative-chain-debug` after successful tests.
- The existing **V3 Emergent Validation** workflow runs its separate regression gates.
- `HiddenNarrativeChainTest`: both displayed variants refer to one Core destination; stale world hashes, altered choice origins, malformed provider output and misleading system-style narration are rejected.
- `FullGameplay200TurnSimulationTest`: baseline 200 live turns exercise real Entity encounters and Poker Dice. Separate 200-seed comparison evaluates canonical world equality when the visible player action and GM prose differ; the multi-beat test checks that preview hashes still match actual Core commits after narration.
- `narrative-metrics.test.cjs`: validates local parser against synthetic debug traces.

These checks verify mechanics and structural constraints, **not** whether Gemini Low produces convincing Vietnamese prose or whether a real player notices convergence.

## Live Gemini Low playtest

Install the debug APK artifact from the successful PR workflow on a test device. Configure provider keys through the existing private build mechanism; **never** commit API keys, include them in test logs, or upload a secret-bearing test APK publicly. The provider model is the existing project's `gemini-3.8-flash` with `thinkingLevel=low`. Confirm the actual provider from device logs rather than assuming the Haiku fallback did not run.

1. Start clean. Perform at least 20 ordinary exploration turns, choosing A and B alternately; include at least five deliberately unexpected freeform actions (sit still, run away, investigate an unexpected object, refuse to move, search backward).
2. Record whether each scripted A/B turn emitted `HIDDEN_CHAIN_CACHE_HIT`, and whether freeform submissions emitted `HIDDEN_CHAIN_PLAYER_ACTION_REWRITE`. Missing hits mean the Core fell back to an uncached narration call, not a zero-call success.
3. Include naturally occurring Entity fights. Verify Poker Dice still operates; on victory the story can resume, while defeat retains real position and route progress and displays an obscured Level/location and snapshot.
4. Compare 20 generated passages for repeated imagery and transition devices, unnatural agency overrides, contradictory known facts, narrative/system-report leakage and moments when the shared destination becomes obvious. Record the exact action, visible passage and issue type without including secrets.
5. Use a separate blinded reader who does not know that A and B share a destination. Ask them whether actions seemed acknowledged and when they inferred convergence. Do not infer player deception from automated guard success.
6. Repeat after adjusting prompt design. Compare actual API attempts and recorded token usage rather than estimating savings from planned beats.

To collect device telemetry (Android SDK `adb` required):

```sh
adb logcat -c
# Play the test session with a DEBUG APK and configured provider.
adb logcat -d -s BackroomMain:D > narrative-session.log
node android-apk/tools/narrative-metrics.cjs narrative-session.log
```

The parser reports canonical turns, cached A/B hits, freeform rewrites, prepared beats, batched provider attempts and token totals where the provider supplies usage metadata. Gemini request retries and Haiku fallback requests are counted as HTTP attempts. Partial logs and failed API responses have no reliable token totals; label these as missing measurements.

## Evaluation record

For each experiment record: exact commit SHA, APK build run, model/fallback actually used, 20-turn transcript without secrets, parser JSON, number of human readers, human-detected convergence and observed contradictions. Only evaluate quota savings relative to a baseline run under the same model/configuration and comparable prompts. Keep the PR in Draft if API savings or literary quality remains unmeasured or regression gates fail.
