#!/usr/bin/env python3
"""Merge completed ignored Korean chunks into values-ko/strings.xml."""
import copy
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/res/values/strings.xml"
CHUNKS = ROOT / "work/ko"
TARGET = ROOT / "app/src/main/res/values-ko/strings.xml"
TOOLS = "{http://schemas.android.com/tools}"


def text(element):
    return "".join(element.itertext())


def indent(element, level=0):
    whitespace = "\n" + "  " * level
    if len(element):
        if not element.text or not element.text.strip():
            element.text = whitespace + "  "
        for child in element:
            indent(child, level + 1)
        if not element[-1].tail or not element[-1].tail.strip():
            element[-1].tail = whitespace
    if level and (not element.tail or not element.tail.strip()):
        element.tail = whitespace


def main() -> int:
    source_root = ET.parse(SOURCE).getroot()
    source = [e for e in source_root if e.attrib.get("translatable") != "false" and e.attrib.get("name")]
    translated = {}
    for path in sorted(CHUNKS.glob("chunk-*.xml")):
        for element in ET.parse(path).getroot():
            name = element.attrib.get("name")
            if name in translated:
                print(f"duplicate chunk key: {name}", file=sys.stderr)
                return 1
            translated[name] = element
    errors = []
    root = ET.Element("resources")
    for element in source:
        name = element.attrib["name"]
        candidate = translated.get(name)
        if candidate is None or not text(candidate).strip():
            errors.append(f"missing translation: {name}")
            continue
        merged = copy.deepcopy(candidate)
        for attr in tuple(merged.attrib):
            if attr.startswith(TOOLS):
                del merged.attrib[attr]
        merged.tail = None
        root.append(merged)
    extras = set(translated) - {e.attrib["name"] for e in source}
    errors.extend(f"unknown chunk key: {name}" for name in sorted(extras))
    if errors:
        print("Cannot merge Korean chunks:", file=sys.stderr)
        print("\n".join(errors), file=sys.stderr)
        return 1
    TARGET.parent.mkdir(parents=True, exist_ok=True)
    indent(root)
    ET.ElementTree(root).write(TARGET, encoding="utf-8", xml_declaration=True)
    print(f"wrote {len(source)} Korean strings to {TARGET}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
