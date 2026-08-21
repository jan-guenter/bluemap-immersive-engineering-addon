#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated formed-structure gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")

    if tuple(page.number for page in cases.PAGES) != (2, 3, 4, 5):
        raise ValueError("gallery must contain pages 2-5 in order")
    case_ids = tuple(case.case_id for case in cases.PLACEMENTS)
    if len(case_ids) != 47 or len(set(case_ids)) != len(case_ids):
        raise ValueError("gallery must contain 47 uniquely named structure cases")
    if any(case.expected != "formed-visible" for case in cases.PLACEMENTS):
        raise ValueError("every structure case must expect a formed visible model")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^place template ", functions, re.MULTILINE)) != 47:
        raise ValueError("gallery must issue one installed-template placement per case")
    if len(
        re.findall(
            rf"^function {re.escape(cases.HELPER_NAMESPACE)}:form$",
            functions,
            re.MULTILINE,
        )
    ) != 47:
        raise ValueError("gallery must issue one helper formation request per case")
    if len(
        re.findall(
            rf"^function {re.escape(cases.HELPER_NAMESPACE)}:verify$",
            functions,
            re.MULTILINE,
        )
    ) != 47:
        raise ValueError("gallery must issue one helper verification request per case")

    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")

    bundled_nbt = tuple(ROOT.rglob("*.nbt"))
    if bundled_nbt:
        raise ValueError("gallery must not bundle Immersive Engineering templates")
    local_ie_data = ROOT / "datapack/data/immersiveengineering"
    if local_ie_data.exists():
        raise ValueError("gallery must reference, not copy, installed IE data")

    for case in cases.PLACEMENTS:
        bounds = case.clear_bounds()
        volume = (
            (bounds[3] - bounds[0] + 1)
            * (bounds[4] - bounds[1] + 1)
            * (bounds[5] - bounds[2] + 1)
        )
        if volume > 32_768:
            raise ValueError(f"clear command exceeds the fill limit: {case.case_id}")

    print(
        "formed gallery lint passed: 24 IDs, 14 mirrored variants, "
        "9 east controls, 47 cases"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
