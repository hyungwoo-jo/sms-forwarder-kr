#!/usr/bin/env python3
"""Validate Korean-only application resources and template compatibility."""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
HAN = re.compile(r"[\u3400-\u9fff]")
errors = []
base = ET.parse(ROOT / "app/src/main/res/values/strings.xml").getroot()
strings = [e for e in base if e.tag == "string"]
keys = [e.attrib["name"] for e in strings]
expected = json.loads((ROOT / "docs/resource-keys.json").read_text())
if sorted(keys) != expected or len(keys) != len(set(keys)):
    errors.append("Missing, unexpected or duplicate string keys")
for path in (ROOT / "app/src/main/res").rglob("*.xml"):
    root = ET.parse(path).getroot()
    for element in root.iter():
        if HAN.search(element.text or "") or any(HAN.search(v) for v in element.attrib.values()):
            errors.append(f"Chinese UI resource: {path.relative_to(ROOT)}: {element.attrib.get('name', element.tag)}")
for element in strings:
    if not "".join(element.itertext()).strip():
        errors.append(f"Empty string: {element.attrib['name']}")
if any((ROOT / "app/src/main/res").glob("values-zh*")):
    errors.append("Chinese locale resources remain")
if errors:
    raise SystemExit("\n".join(errors))
print(f"Korean-only localization check passed: {len(strings)} keys, no Chinese UI resources")
