#!/usr/bin/env python3
"""Build and validate BACKROOMS runtime canon assets from root canon Markdown."""
from __future__ import annotations

import argparse
import copy
import json
import re
import sys
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
CANON = ROOT / "canon"
GENERATED = {"generator": "tools/canon.py", "doNotEdit": True, "schemaVersion": 1}

RUNTIME_SOURCES = {
    CANON / "runtime/characters.md": ROOT / "android-apk/app/src/main/assets/knowledge/characters_current.json",
    CANON / "runtime/entities.md": ROOT / "android-apk/app/src/main/assets/knowledge/entity_encounters.json",
    CANON / "runtime/levels.md": ROOT / "android-apk/app/src/main/assets/knowledge/level_knowledge.json",
    CANON / "runtime/knowledge.md": ROOT / "android-apk/app/src/main/assets/knowledge/knowledge_db.json",
    CANON / "runtime/level_graph.md": ROOT / "android-apk/app/src/main/assets/level_graph.json",
}
PROSE_SOURCES = {
    CANON / "characters/lucia_codex.md": ROOT / "android-apk/app/src/main/assets/canon/Lucia_Codex.md",
    CANON / "characters/luc_tram_codex.md": ROOT / "android-apk/app/src/main/assets/canon/Lục_Trầm_Codex.md",
    CANON / "world/entities_visual.md": ROOT / "android-apk/app/src/main/assets/canon/Entity.md",
}
INDEX_OUT = ROOT / "android-apk/app/src/main/assets/canon/canon_index.json"
REPORT_OUT = ROOT / "android-apk/app/src/main/assets/canon/canon_validation.json"
REQUIRED_META = {"id", "owner", "type", "status", "sourceRef", "revision", "scope", "knownBy"}
VALID_TYPES = {"FOUNDATION", "GAMEPLAY", "CAMPAIGN", "KNOWLEDGE"}
VALID_STATUS = {"CURRENT", "DYNAMIC", "DISPUTED", "OPEN", "UNKNOWN", "LEGACY"}


def extract_json(path: Path) -> dict[str, Any]:
    text = path.read_text(encoding="utf-8")
    match = re.search(r"```json\s*\n(.*?)\n```", text, re.S)
    if not match:
        raise ValueError(f"{path.relative_to(ROOT)}: missing fenced JSON payload")
    value = json.loads(match.group(1))
    if not isinstance(value, dict):
        raise ValueError(f"{path.relative_to(ROOT)}: runtime payload must be an object")
    return value


def clean(value: Any) -> Any:
    if isinstance(value, dict):
        return {k: clean(v) for k, v in value.items() if not k.startswith("_canon")}
    if isinstance(value, list):
        return [clean(v) for v in value]
    if isinstance(value, float) and value.is_integer():
        return int(value)
    return value


def generated_json(source: Path, value: dict[str, Any]) -> str:
    payload = {"_generated": {**GENERATED, "source": str(source.relative_to(ROOT))}}
    payload.update(clean(value))
    return json.dumps(payload, ensure_ascii=False, indent=2) + "\n"


def prompt_text(parent: dict[str, Any], meta_key: str) -> str:
    if meta_key != "_canon":
        if meta_key == "_canonGameplay":
            gameplay = parent.get("gameplay", parent.get("autoProcSkills", {}))
            return json.dumps(clean(gameplay), ensure_ascii=False, separators=(",", ":"))
        return ""
    if isinstance(parent.get("text"), str):
        return parent["text"].strip()
    if "runtime" in parent:
        fields = ["name", "runtime", "abilities", "equipment", "backrooms", "relationship", "dialogue", "visual"]
        return "\n".join(str(parent.get(key, "")).strip() for key in fields if str(parent.get(key, "")).strip())
    if "canon" in parent:
        return (str(parent.get("name", "")).strip() + "\n" + str(parent.get("canon", "")).strip()).strip()
    if "canonicalFacts" in parent or "identity" in parent:
        pieces = [str(parent.get("name", "")).strip()]
        for key, limit in (("identity", 4), ("canonicalFacts", 6), ("gmConstraints", 3), ("forbiddenInventions", 2)):
            values = parent.get(key, [])
            if isinstance(values, list):
                pieces.extend(str(v).strip() for v in values[:limit] if str(v).strip())
        return "\n".join(p for p in pieces if p)
    if "displayName" in parent:
        return "\n".join(str(parent.get(k, "")).strip() for k in ("displayName", "defaultLocation") if str(parent.get(k, "")).strip())
    return ""


def normalize_meta(raw : dict[str, Any], parent: dict[str, Any], meta_key: str, source_file: Path) -> dict[str, Any]:
    meta = copy.deepcopy(raw)
    missing = sorted(REQUIRED_META - meta.keys())
    if missing:
        raise ValueError(f"{source_file.relative_to(ROOT)}: {meta.get('id','<missing-id>')} missing metadata {missing}")
    if meta["type"] not in VALID_TYPES:
        raise ValueError(f"{meta['id']}: invalid type {meta['type']}")
    if meta["status"] not in VALID_STATUS:
        raise ValueError(f"{meta['id']}: invalid status {meta['type']}")
    for key in ("scope", "requires", "refs"):
        value = meta.get(key, [])
        if isinstance(value, str):
            value = [value] if value else []
        meta[key] = list(value)
    meta["core"] = bool(meta.get("core", False))
    meta["promptEligible"] = meta.get("knownBy") not in {"SECRET", "SYSTEM"} and meta.get("status") not in {"LEGACY"}
    meta["sourceFile"] = str(source_file.relative_to(ROOT))
    meta["text"] = prompt_text(parent, meta_key)
    return meta


def collect_json_items(source_file: Path, value: Any) -> list[dict[str, Any]]:
    items: list[dict[str, Any]] = []
    def walk(node: Any) -> None:
        if isinstance(node, dict):
            for key, raw in node.items():
                if key.startswith("_canon") and isinstance(raw, dict):
                    items.append(normalize_meta(raw, node, key, source_file))
            for key, child in node.items():
                if not key.startswith("_canon"):
                    walk(child)
        elif isinstance(node, list):
            for child in node:
                walk(child)
    walk(value)
    return items


def parse_scalar(text: str) -> Any:
    value = text.strip()
    if not value:
        return ""
    if value.lower() in {"true", "false"}:
        return value.lower() == "true"
    if value.startswith("["):
        return json.loads(value)
    return value


def collect_markdown_items() -> list[dict[str, Any]]:
    items: list[dict[str, Any]] = []
    pattern = re.compile(r"<!-- canon-item\s*\n(.*?)\n-->\s*\n(.*?)\(?=<!-- canon-item\s*\n|\Z)", re.S)
    for path in sorted(CANON.rglob("*.md")):
        if path in RUNTIME_SOURCES or path in PROSE_SOURCES or path.name in {"README.md", "CONFLICTS.md"}:
            continue
        text = path.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            raw_meta: dict[str, Any] = {}
            for line in match.group(1).splitlines():
                if not line.strip() or line.lstrip().startswith("#"):
                    continue
                key, sep, value = line.partition(":")
                if not sep:
                    raise ValueError(f"{path.relative_to(ROOT)}: malformed metadata line {line!r}")
                raw_meta[key.strip()] = parse_scalar(value)
            body = match.group(2).strip()
            parent = {"text": body}
            items.append(normalize_meta(raw_meta, parent, "_canon", path))
    return items


def source_warning(meta: dict[str, Any]) -> str | None:
    kind = str(meta.get("sourceKind", "external")).lower()
    availability = str(meta.get("sourceAvailability", "")).upper()
    ref = str(meta.get("sourceRef", "")).strip()
    if kind == "repo":
        local = ref.split("#", 1)[0]
        if not local or not (ROOT / local).exists():
            return f"{meta['id']}: missing repo source {ref}"
    if availability in {"EXTERNAL_SNAPSHOT", "MISSING_ORIGIN"}:
        return f"{meta['id']}: source availability={availability} ({ref})"
    return None


def build_index(runtime_values: dict[Path, dict[str, Any]]) -> tuple[dict[str, Any], list[str]]:
    items: list[dict[str, Any]] = []
    for source, value in runtime_values.items():
        items.extend(collect_json_items(source, value))
    items.extend(collect_markdown_items())
    by_id: dict[str, dict[str, Any]] = {}
    for item in items:
        item_id = str(item["id"]).strip()
        if not item_id:
            raise ValueError("empty canon id")
        if item_id in by_id:
            raise ValueError(f"duplicate canon id: {item_id}")
        by_id[item_id] = item
    for item in items:
        for ref in item.get("requires", []):
            if ref not in by_id:
                raise ValueError(f"broken required ref: {item['id']} -> {ref}")
        for ref in item.get("refs", []):
            if ref and ref not in by_id:
                raise ValueError(f"broken canon ref: {item['id']} -> {ref}")
    warnings = sorted(filter(None, (source_warning(item) for item in items)))
    items.sort(key=lambda item: item["id"])
    return {
        "schemaVersion": 1,
        "_generated": {**GENERATED, "source": "canon/"},
        "items": items,
    }, warnings


def assert_conflict_guards(runtime_values: dict[Path, dict[str, Any]], outputs: dict[Path, str], index: dict[str, Any]) -> None:
    chars_source = next(v for p, v in runtime_values.items() if p.name == "characters.md")
    chars = clean(chars_source)["characters"]
    ultimate = chars["cao_minh"]["gameplay"]["ultimate"]
    if ultimate.get("hits") != 24 or ultimate.get("baseDamagePercentPerHit") != 100 or ultimate.get("bonusDamagePercentPerHit") != 15:
        raise ValueError("Cao Minh Ultimate projection must be 24 x (100% current DMG + 15% Bonus DMG)")
    if "damageHpPerHit" in ultimate:
        raise ValueError("stale fixed Cao Minh Ultimate damageHpPerHit is forbidden")
    core = (ROOT / "android-apk/app/src/main/java/com/rabpit/backroom/core/CombatChoiceEngine.java").read_text(encoding="utf-8")
    expected_constants = {
        "HUYET_MA_24_HIT_COUNT": 24,
        "ULTIMATE_BONUS_DAMAGE_PERCENT": 15,
        "LUCIA_TOO_YOUNG_TO_DIE_SHOT_COUNT": 60,
        "LUC_TRAM_THIEN_KIEM_HIT_COUNT": 60,
    }
    for name, expected in expected_constants.items():
        match = re.search(rf"\b{name}\s*=\s*(\d+)\s*;", core)
        if not match or int(match.group(1)) != expected:
            raise ValueError(f"gameplay projection drift: {name} != {expected}")
    knowledge = clean(next(v for p, v in runtime_values.items() if p.name == "knowledge.md"))
    record = next((r for r in knowledge.get("records", []) if r.get("id") == "CHAR.CAO_MINH.HUYET_MA_24"), None)
    if not record or "100%" not in record.get("text", "") or "15%" not in record.get("text", ""):
        raise ValueError("knowledge_db Cao Minh Ultimate record drifted from Core formula")
    luc = outputs[PROSE_SOURCES[CANON / "characters/luc_tram_codex.md"]]
    stale = "Override: Retcon này thay thế toàn bộ Character Canon cũ của Lucia Lục / Hứa Thuý Mai đối với nhân vật này."
    if stale in luc or "runtime id `lucia`" not in luc or "runtime id `luc_tram`" not in luc:
        raise ValueError("Lucia/Lục Trầm identity guard failed")
    ids = {item["id"] for item in index["items"]}
    if "character.lucia.foundation" not in ids or "character.luc_tram.foundation" not in ids:
        raise ValueError("Lucia and Lục Trầm must remain distinct canon IDs")
    secret = next((item for item in index["items"] if item["id"] == "knowledge.luc_tram.tang_kiem_coc.backstage"), None)
    if not secret or secret.get("knownBy") != "SECRET" or secret.get("promptEligible"):
        raise ValueError("Táng Kiếm Cốc backstage truth must be excluded from narration prompt")


def expected_outputs() -> tuple[dict[Path, str], dict[str, Any], list[str]]:
    runtime_values = {source: extract_json(source) for source in RUNTIME_SOURCES}
    outputs = {target: generated_json(source, value) for source, target in RUNTIME_SOURCES.items() for value in [runtime_values[source]]}
    for source, target in PROSE_SOURCES.items():
        outputs[target] = (
            f"<!-- GENERATED FILE. DO NOT EDIT. Source: {source.relative_to(ROOT)}; run python3 tools/canon.py generate. -->\n"
            + source.read_text(encoding="utf-8").rstrip() + "\n"
        )
    index, warnings = build_index(runtime_values)
    outputs[INDEX_OUT] = json.dumps(index, ensure_ascii=False, indent=2) + "\n"
    report = {
        "schemaVersion": 1,
        "_generated": {**GENERATED, "source": "canon/"},
        "warnings": warnings,
        "counts": {
            "items": len(index["items"]),
            "foundation": sum(i["type"] == "FOUNDATION" for i in index["items"]),
            "gameplay": sum(i["type"] == "GAMEPLAY" for i in index["items"]),
            "campaign": sum(i["type"] == "CAMPAIGN" for i in index["items"]),
            "knowledge": sum(i["type"] == "KNOWLEDGE" for i in index["items"]),
        },
    }
    outputs[REPORT_OUT] = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    assert_conflict_guards(runtime_values, outputs, index)
    return outputs, index, warnings


def generate() -> int:
    outputs, _, warnings = expected_outputs()
    for path, content in outputs.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
    for warning in warnings:
        print(f"canon warning: {warning}", file=sys.stderr)
    print(f"generated {len(outputs)} canon runtime outputs")
    return 0


def check() -> int:
    outputs, index, warnings = expected_outputs()
    drift = []
    for path, expected in outputs.items():
        actual = path.read_text(encoding="utf-8") if path.exists() else None
        if actual != expected:
            drift.append(str(path.relative_to(ROOT)))
    if drift:
        raise ValueError("generated canon output drift: " + ", ".join(drift))
    for warning in warnings:
        print(f"canon warning: {warning}", file=sys.stderr)
    print(f"canon check ok: {len(index['items'])} stable items, {len(warnings)} declared source warnings")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=("generate", "check"))
    args = parser.parse_args()
    try:
        return generate() if args.command == "generate" else check()
    except (ValueError, KeyError, json.JSONDecodeError) as error:
        print(f"canon validation failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
