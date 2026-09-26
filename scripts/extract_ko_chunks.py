#!/usr/bin/env python3
"""Create ignored, ordered Korean translation chunks from Android resources."""
import copy
import shutil
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/res/values/strings.xml"
ENGLISH = ROOT / "app/src/main/res/values-en/strings.xml"
OUTPUT = ROOT / "work/ko"
CHUNK_SIZE = 200


def items(path: Path):
    root = ET.parse(path).getroot()
    return {
        element.attrib["name"]: element
        for element in root
        if element.attrib.get("name")
    }


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


def main():
    source = items(SOURCE)
    english = items(ENGLISH)
    targets = [
        element for element in source.values()
        if element.attrib.get("translatable") != "false"
    ]
    if OUTPUT.exists():
        shutil.rmtree(OUTPUT)
    OUTPUT.mkdir(parents=True)
    for number, start in enumerate(range(0, len(targets), CHUNK_SIZE), 1):
        root = ET.Element("resources")
        for element in targets[start:start + CHUNK_SIZE]:
            copied = copy.deepcopy(element)
            copied.text = ""
            copied.tail = None
            english_value = english.get(element.attrib["name"])
            if english_value is not None:
                copied.set("tools:english", "".join(english_value.itertext()))
            root.append(copied)
        root.set("xmlns:tools", "http://schemas.android.com/tools")
        indent(root)
        ET.ElementTree(root).write(
            OUTPUT / f"chunk-{number:02d}.xml", encoding="utf-8", xml_declaration=True
        )
    print(f"created {number} chunks for {len(targets)} translation keys in {OUTPUT}")


if __name__ == "__main__":
    main()
