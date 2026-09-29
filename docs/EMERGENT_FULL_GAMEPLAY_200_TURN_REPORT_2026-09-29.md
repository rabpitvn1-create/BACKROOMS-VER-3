# V3 Emergent Event System — full-gameplay 200-action simulation (2026-09-29)

## Scope and reproducibility

Repository: `rabpitvn1-create/BACKROOMS-VER-3`; working branch: `codex/ponytail-abc-prefetch-v3`.
This is **not** a Director-only mock: `FullGameplay200TurnSimulationTest` calls
`GameCoreFacade.processRule` → `completePreparedTurn` → actor-safe
`GmNarrativePacket.build` → `NarrationGuard.validate` → `commitNarration`,
then `startCombatRuntime` → real `combatFinishRuntime` /
`processCombatResolution` for each committed Entity encounter.
It loads the checked-in level graph, level knowledge and all 36 entity encounter
entries, retains Core RNG and real state/event/Thread projections, and never calls
Gemini/Haiku. The deterministic valid narration test stub does not assess prose quality.

Each profile performs **200 explorer actions**. Combat hands are additional actual
Core commits and can advance `state.turn`; they are not counted as explorer actions.
Two fixed save IDs: `full-natural-20260929` (new game from Level 0) and
`full-party-20260929` (Lucia already recruited through the existing Character rule).
The test also replays the natural profile's first 30 actions to verify deterministic
results, validates that every selected Entity survives narration and leads to an
actual battle, and checks replay watermarks after each action.

- **Before pacing adjustment, after repairing phantom encounter lifecycle**:
  [successful CI run](https://github.com/rabpitvn1-create/BACKROOMS-VER-3/actions/runs/36518131640)
  at commit `9af2e1ad24544ac64f6a3e332eb40972eeeefff2`.
- **After pacing adjustment**:
  [successful CI run](https://github.com/rabpitvn1-create/BACKROOMS-VER-3/actions/runs/36518713337)
  at commit `95bde726764e993fcaee80ff947a9ce29585806b`.
- Both CI runs passed Node choice-UI verification, Gradle JVM unit tests (including
  the complete two-profile simulation) and `:app:assembleDebug`.

## Measured event distribution

Numbers are actual selected families or committed event counts, not predicted
probabilities. Both profiles use the same fixed seed before/after, but changing the
number of combat commits changes later state-version-dependent RNG keys; treat this
as two diagnostic scenarios, **not** a controlled stochastic A/B estimate.

| Observed in 200 explorer actions | Natural before | Natural after | Lucia before | Lucia after |
|---|---:|---:|---:|---:|
| Real Entity encounters and combat starts | 93 | 70 | 101 | 67 |
| Combat hands resolved | 135 | 98 | 168 | 89 |
| Combat victories / defeats | 93 / 0 | 70 / 0 | 101 / 0 | 67 / 0 |
| Anomaly setups | 11 | 11 | 10 | 12 |
| Anomaly payoff events | 5 | 3 | 4 | 7 |
| Resource/action echoes | 2 | 5 | 4 | 2 |
| Relationship beats | 7 | 9 | 4 | 5 |
| Quiet beats | 26 | 29 | 26 | 25 |
| Chest selections | 4 | 6 | 5 | 3 |
| No selected event (NONE) | 51 | 66 | 46 | 78 |
| Level transitions | 1 | 1 | 2 | 0 |
| Unresolved Threads at finish | 3 | 3 | 2 | 3 |
| Accumulated unresolved payoff debt | 301 | 235 | 198 | 73 |

Across the two 200-action profiles, the encounter total fell from **194 to 137
(29.4% fewer)**. Real-combat frequency per explorer action fell from **48.5%
to 34.25%** (natural: 46.5%→35%; established party: 50.5%→33.5%).
Combined relationship beats rose from 11 to 14; quiet beats 52 to 54;
anomaly payoff plus resource-echo consequences 15 to 17. These are descriptive
counts for two seeds only, not promises about long-run balance.

The real tests also exposed a previous false positive: without repairing the
terminal-combat lifecycle, the system committed 135 Entity events in the natural
profile but started only **one** actual battle. That output was rejected as an
invalid gameplay-frequency measurement. The repaired before/after runs verify
exact agreement between committed Entity events and battles actually started.

## Root causes and minimal fixes

1. **Phantom encounters**: a previously completed `combat` object remained in
   state. Normalization performed during narration commit cleared the
   `entityEncounterKey` of a newly committed encounter. `EntityCore` now
   retires the terminal combat object when a *new eligible* Entity event is
   activated, and rejects activation while another encounter/combat is active.
   `EntityCoreTest` and the full simulation check the behavior.
2. **Authoritative replay failures under long runs**: patch verification used
   string serialization to compare JSON objects and strict boxed Number equality;
   equivalent JSON with reordered keys or deserialized numeric types could
   incorrectly fail. `AuthoritativeStatePatch` now compares object members
   recursively, preserves array order and compares JSON numbers semantically.
   A focused regression test covers both object order and numeric widening.
3. **Combat-heavy pacing**: the checked-in encounter registry contains 36
   auto-spawn entries with nominal percentages totaling 112.85 when added. That
   sum is **not** the actual encounter probability: existing Core competition,
   category modifiers and RNG determine the selected event. Changing all entity
   rates would have a broad gameplay impact. Instead, the Director's already
   replayable `DANGER_UNTIL_TURN` cooldown now grants one Entity-candidate-free
   explorer action after a resolved fight. An authoritative active world-alert
   consequence can override this recovery beat. Existing per-eligible
   entity rates remain unchanged.

## Pacing, remaining risks and interpretation

- The adjustment measurably reduced combat without prescribing a fixed storyline,
  inventing resources, using the LLM to choose mechanics, or breaking Core
  replay. The initial pass deliberately stops here rather than overfitting two
  deterministic seeds.
- **Roughly one in three explorer actions can still select an Entity** in these
  fixed runs. Minimum observed gap between Entity selections remains one world
  action: the intentional active world-alert override permits urgency. Consider
  additional seeds and player-policy variation before selecting a tighter target
  or lengthening the recovery beat.
- Both tuned profiles recorded **zero player defeats** (137 wins), despite the
  harness immediately finalizing each initial Poker Dice hand without strategic
  rerolls. This flags a separate combat-difficulty/playtesting question, not a
  justification for silently changing canon hero stats or encounter combat rules.
- The shipped all-level roaming policy allows **Hui bosses at Level 0**; the
  simulation encountered them there. Confirm desired canon/design restrictions
  with the owner before changing their eligibility or spawn rates.
- Level travel is stochastic; the Level 0 route can reset repeatedly. The
  natural tuned run reached `hua_1900_0`, while the established-party tuned
  run remained on Level 0 for 200 actions. This pair of samples cannot establish
  cross-level event balance. Deeper-level scenario testing is a separate task.
- The simulation exercises the actual Core and narration contract but uses a
  deterministic, structurally valid narration stub; it does **not** measure
  AI prose quality, provider latency, or subjective player experience.

## Code and tests

Relevant changes are in
`core/EntityCore.java`, `core/LevelCore.java`,
`core/GameCoreFacade.java`, `core/EmergentTurnEngine.java`,
`core/AuthoritativeStatePatch.java`, and focused tests
`core/EntityCoreTest.java`, `core/EmergentDirectorTest.java`,
`core/EmergentTurnEngineTest.java` plus
`core/FullGameplay200TurnSimulationTest.java`.
No V2 repository changes and no merge to `main`.
