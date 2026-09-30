# Canon authoring

`canon/` is the repository editing authority for static canon and its runtime projections. Do not hand-edit generated files under `android-apk/app/src/main/assets/canon/`, `assets/knowledge/` or the generated `assets/level_graph.json`. The generator writes a `_generated` marker to JSON outputs and a generated-file comment to Markdown outputs.

The current flow is:

```text
canon/*.md
   │
   ├─ tools/canon.py generate
   │    ├─ assets/canon/canon_index.json
   │    ├─ assets/canon/canon_validation.json
   │    ├─ assets/canon/{Entity,Lucia_Codex,Lục_Trầm_Codex}.md
   │    ├─ assets/knowledge/{characters_current,entity_encounters,level_knowledge,knowledge_db}.json
   │    └─ assets/level_graph.json
   │
   └─ Core loaders + CanonRetriever -> committed scene -> filtered GM narrative packet
```

## Canon classes and authority

**FOUNDATION** describes stable identity, history, appearance, capabilities and world facts. It is baseline, not mutable save state.

**GAMEPLAY** describes mechanical projections such as damage, proc, spawn eligibility and Level routing. Java Core remains authoritative for actual mechanics and committed outcomes. Canon projections must match Core; the validator checks the locked Ultimate values that previously drifted.

**CAMPAIGN** is dynamic continuity already committed by Core: save state, Party, inventory, injuries, relationship state, knowledge learned, promises and consequences. Static canon generation never creates or resets campaign state.

**KNOWLEDGE** records what an actor observed, was told, believes, inferred or verified. A true backstage fact and a character belief are separate items.

Authority is based on the owning source and explicit revision, not file modification time. A retcon must say what owner/item it replaces and have evidence in the authoritative source. `OPEN` and `UNKNOWN` stay unresolved; do not infer a convenient answer. A conflict without enough evidence is marked `DISPUTED`/OPEN, current runtime behavior is preserved, and the issue is documented in `canon/CONFLICTS.md`.

Core owns damage/proc/spawn/loot/Party/route/outcome. AI narration may describe a committed result but cannot create one. Relationship-development notes are constraints/possibilities, not a mandatory script.

## Stable metadata

Runtime source Markdown contains fenced JSON because several existing loaders consume JSON projections. Each top-level canon record carries a private `_canon` object; the generator strips these authoring fields from the legacy projection and writes them into the stable runtime index.

Required metadata:

- `id`: stable ID. Never derive identity from filename or heading and never reuse an ID for a different fact.
- `owner`: authority owner such as `character:luc_tram`, `world:level:0` or `core:combat`.
- `type`: `FOUNDATION`, `GAMEPLAY`, `CAMPAIGN` or `KNOWLEDGE`.
- `status`: `CURRENT`, `DYNAMIC`, `DISPUTED`, `OPEN`, `UNKNOWN` or `LEGACY`.
- `sourceRef` and `revision`: provenance and revision actually used.
- `sourceKind` / `sourceAvailability`: whether the source is in-repo or an explicitly declared external/missing-origin snapshot.
- `scope`: scene subjects to which the item applies, for example `character:luc_tram` or `level:0`.
- `knownBy`: knowledge gate.
- `requires`: hard stable-ID dependencies. Missing dependencies invalidate the generated index.
- `refs`: other stable canon references. Broken references invalidate the generated index.
- `core`: whether this is the mandatory baseline item for its scene subject.

Example:

```json
"_canon": {
  "id": "character.lucia.foundation",
  "owner": "character:lucia",
  "type": "FOUNDATION",
  "status": "CURRENT",
  "sourceRef": "canon/characters/lucia_codex.md",
  "revision": "R03",
  "scope": ["character:lucia"],
  "knownBy": "SCENE",
  "sourceKind": "repo",
  "sourceAvailability": "AVAILABLE",
  "refs": [],
  "requires": [],
  "core": true
}
```

Free-form Markdown knowledge items use the `<!-- canon-item ... -->` metadata form; `canon/knowledge/tang_kiem_coc.md` is the concrete example.

## Knowledge gates

`SCENE`/ `PUBLIC` exposes a prompt-eligible item only when its scope is active. `OWNER` exposes baseline knowledge only while that character is part of the current/pending scene. `BELIEF:<claimId>` requires a matching committed belief for the owner. `FACT:<factId>` requires the owner's belief to point at that confirmed fact. `SECRET` is never sent to AI narration. `SYSTEM` is validation/runtime metadata and is also never sent.

Táng Kiếm Cốc is deliberately split into reported material, direct observation, Lục Trầm's belief/inference, writer-secret backstage truth, and an OPEN item. Do not collapse those records into one paragraph or promote the writer-secret record into character knowledge.

## Editing workflow

1. Edit only the relevant file under `canon/`. For generated runtime datasets, edit the fenced JSON in `canon/runtime/*.md`; for prose Codex edit `canon/characters/` or `canon/world/`.
2. Keep existing stable IDs when changing the same fact. For a genuinely new item, add a new unique ID and explicit provenance.
3. If a source is external and not checked in, keep `sourceAvailability: EXTERNAL_SNAPSHOT` (or `MISSING_ORIGIN` for a stale repository reference). Do not pretend it is locally verified; the warning is written to `assets/canon/canon_validation.json`.
4. Run:

```bash
python3 tools/canon.py generate
python3 tools/canon.py check
node android-apk/app/src/test/js/gm-choice-ui.test.cjs
cd android-apk
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon
```

`check` fails on duplicate stable IDs, broken `requires`/canon refs, missing required repo source paths, generated-output drift, Lucia/Lục Trầm identity regression, Táng Kiếm Cốc secret exposure and the locked Cao Minh/Lucia/Lục Trầm Ultimate projection values.

## Verifying runtime integration

After generation, inspect `android-apk/app/src/main/assets/canon/canon_index.json` for the stable ID and its `promptEligible`/knowledge gate. `CanonRetriever.fromAssets` loads that index first. For a committed scene it requires a `core: true` item for the current Level, Cao Minh, every joined/pending character and any active Entity. Required dependencies are resolved by stable ID; missing mandatory material causes `MainActivity` to abort narration instead of silently sending an incomplete prompt.

The generated Level/Entity/character JSON is loaded by the existing Core loaders, so a source edit can be checked both in the generated asset and in the Core unit tests. `CanonRetrieverStableIndexTest` covers stable IDs, pending-character retrieval, belief gating, knowledge-leak prevention, dependency reporting and read-only state behavior.

## Save compatibility

Canon generation never writes save data. FOUNDATION items are prompt/runtime baselines only. Do not add code that copies foundation defaults over an existing save. New state migrations belong in Core migration code and must preserve existing Party, inventory, injuries, relationships and learned knowledge unless an explicitly approved migration says otherwise. Stable canon IDs are independent of filenames/headings so documentation reorganization does not require save migration.
