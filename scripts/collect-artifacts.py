#!/usr/bin/env python3
"""Verify signed release APKs and collect local distribution records."""
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIST = ROOT / "dist"
BUILD_TOOLS = Path(os.environ["ANDROID_HOME"]) / "build-tools/33.0.1"
def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True).strip()
metadata = json.loads((ROOT / "build/app/outputs/apk/release/output-metadata.json").read_text())
DIST.mkdir(exist_ok=True)
expected_files = {
    e["outputFile"] for e in metadata["elements"]
    if not e["filters"] or any(f.get("value") == "arm64-v8a" for f in e["filters"])
}
for previous in DIST.glob("*.apk"):
    if previous.name not in expected_files:
        archive = DIST / "archive"
        archive.mkdir(exist_ok=True)
        shutil.move(str(previous), str(archive / previous.name))
artifacts = []
for element in metadata["elements"]:
    abi = next((f["value"] for f in element["filters"] if f["filterType"] == "ABI"), "universal")
    if abi not in ("universal", "arm64-v8a"):
        continue
    apk = ROOT / "build/app/outputs/apk/release" / element["outputFile"]
    cert = run(str(BUILD_TOOLS / "apksigner"), "verify", "--verbose", "--print-certs", str(apk))
    badging = run(str(BUILD_TOOLS / "aapt"), "dump", "badging", str(apk))
    resources = run(str(BUILD_TOOLS / "aapt"), "dump", "--values", "resources", str(apk))
    if re.search(r"[\u3400-\u9fff]", resources):
        raise SystemExit("Chinese text found in packaged Android resource table")
    if "application-debuggable" in badging:
        raise SystemExit("Release APK is debuggable")
    android = "{http://schemas.android.com/apk/res/android}"
    tools = "{http://schemas.android.com/tools}"
    source_manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
    allowed_permissions = {e.get(android + "name") for e in source_manifest.findall("uses-permission") if e.get(tools + "node") != "remove"}
    allowed_permissions.add("com.hwserve.smsforwarder.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION")
    actual_permissions = set(re.findall(r"uses-permission: name='([^']+)'", badging))
    if actual_permissions - allowed_permissions:
        raise SystemExit("Unexpected merged APK permissions: " + str(sorted(actual_permissions - allowed_permissions)))
    package = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", badging)
    if not package or package[1] != "com.hwserve.smsforwarder":
        raise SystemExit("Unexpected release application ID")
    certificate = re.search(r"certificate SHA-256 digest: ([0-9a-f]+)", cert)
    if not certificate:
        raise SystemExit("Missing verified signing certificate digest")
    with zipfile.ZipFile(apk) as archive:
        names = archive.namelist()
        if archive.read("assets/LICENSE.txt") != (ROOT / "LICENSE").read_bytes():
            raise SystemExit("Upstream license is not preserved inside APK")
        for name in names:
            if re.fullmatch(r"classes\d*\.dex", name) and b"com/umeng/" in archive.read(name):
                raise SystemExit("Umeng descriptor found in APK DEX")
        if any(name.endswith("/libgojni.so") for name in names):
            raise SystemExit("Removed FRP native library returned")
    digest = hashlib.sha256(apk.read_bytes()).hexdigest()
    shutil.copy2(apk, DIST / apk.name)
    (DIST / f"{abi}-apksigner.txt").write_text(cert + "\n")
    (DIST / f"{abi}-badging.txt").write_text(badging + "\n")
    (DIST / f"{abi}-resources.txt").write_text(resources + "\n")
    artifacts.append({"file": apk.name, "abi": abi, "applicationId": package[1], "versionCode": int(package[2]), "versionName": package[3], "sha256": digest, "signingCertificateSha256": certificate[1]})
if {a["abi"] for a in artifacts} != {"arm64-v8a", "universal"}:
    raise SystemExit("Both required release APK variants were not produced")
info = {
    "upstreamCommit": "a3d23026f0058420869163c1d5dfb463ce52fc15",
    "forkCommit": run("git", "rev-parse", "HEAD"),
    "dirty": bool(run("git", "status", "--porcelain")),
    "jdk": subprocess.check_output([str(Path(os.environ["JAVA_HOME"]) / "bin/java"), "-version"], stderr=subprocess.STDOUT, text=True).strip(),
    "androidBuildTools": "33.0.1", "androidCompileSdk": 33, "gradleWrapper": "7.3.3",
    "deviceValidation": "DEVICE_PENDING", "artifacts": artifacts,
}
(DIST / "BUILD-INFO.json").write_text(json.dumps(info, ensure_ascii=False, indent=2) + "\n")
(DIST / "SHA256SUMS").write_text("".join(f"{a['sha256']}  {a['file']}\n" for a in artifacts))
shutil.copy2(ROOT / "LICENSE", DIST / "LICENSE")
shutil.copy2(ROOT / "docs/README-install-ko.md", DIST / "README-install-ko.md")
shutil.copy2(ROOT / "docs/CHANGES-ko.md", DIST / "CHANGES-ko.md")
print("Verified and collected arm64-v8a and universal signed release APKs in dist/")
