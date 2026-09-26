#!/usr/bin/env python3
"""Static regression checks for removed automatic network paths and stable tags."""
import json
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = "a3d23026f0058420869163c1d5dfb463ce52fc15"
errors = []
for path in (ROOT / "app/src/main/kotlin").rglob("*.kt"):
    source = path.read_text(encoding="utf-8")
    for forbidden in (".ignoreHttpsCert(", "com.umeng", "UMengInit", "XUpdateInit"):
        if forbidden in source:
            errors.append(f"{path.relative_to(ROOT)}: {forbidden}")
expected_tags = json.loads((ROOT / "docs/canonical-tags.json").read_text())
new = ET.parse(ROOT / "app/src/main/res/values/strings.xml").getroot()
def tags(root):
    return {e.attrib["name"]: "".join(e.itertext()) for e in root if e.attrib.get("name", "").startswith("tag_")}
if expected_tags != tags(new):
    errors.append("Canonical template tags changed from the pinned upstream")
for path in (ROOT / "app/src/main/res").glob("values-*/strings.xml"):
    for e in ET.parse(path).getroot():
        if e.attrib.get("name") in tags(new):
            errors.append(f"Locale overrides canonical tag: {path}: {e.attrib['name']}")
settings = (ROOT / "app/src/main/kotlin/cn/ppps/forwarder/utils/SettingUtils.kt").read_text()
http_init = (ROOT / "app/src/main/kotlin/cn/ppps/forwarder/utils/sdkinit/XBasicLibInit.kt").read_text()
if "XHttpSDK.debug()" in http_init or ".debug(false)" not in http_init:
    errors.append("Unredacted XHttp diagnostics were enabled")
if "SharedPreference(SP_ENABLE_PHONE_AREA_LOOKUP, false)" not in settings:
    errors.append("Phone-area lookup default is no longer OFF")
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text()
if "UpdateTipDialog" in manifest or "umeng" in manifest.lower():
    errors.append("Removed SDK/update manifest entry returned")
if errors:
    raise SystemExit("Privacy regression check failed:\n" + "\n".join(errors))
print(f"Privacy regression check passed; {len(tags(new))} canonical tags unchanged")
