#!/usr/bin/env python3
"""Verify the independent notification relay APK identity and contents."""
import os
import re
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APK = ROOT / "build/cleanlite/outputs/apk/release/NotifyRelay_1_release.apk"
TOOLS = Path(os.environ["ANDROID_HOME"]) / "build-tools/36.0.0"
EXPECTED_CERT = "e476e7b37e0119f0b01edd874c3cf3bc88547ec7d322988cfdf5d9813b90e710"

def run(*command):
    return subprocess.check_output(command, text=True)

badging = run(str(TOOLS / "aapt"), "dump", "badging", str(APK))
certs = run(str(TOOLS / "apksigner"), "verify", "--verbose", "--print-certs", str(APK))
assert "name='com.hwserve.notifyrelay' versionCode='1'" in badging
assert "targetSdkVersion:'36'" in badging
assert "certificate SHA-256 digest: " + EXPECTED_CERT in certs
assert "Verified using v2 scheme (APK Signature Scheme v2): true" in certs
permissions = set(re.findall(r"uses-permission: name='([^']+)'", badging))
assert permissions == {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"}, permissions
assert "provides-component:'notification-listener'" in badging
with zipfile.ZipFile(APK) as archive:
    assert not any(name.startswith("lib/") for name in archive.namelist())
    dex = b"".join(archive.read(name) for name in archive.namelist() if re.fullmatch(r"classes\d*\.dex", name))
    for forbidden in (b"cn/ppps", b"sms_forwarder.db", b"SmsReceiver", b"HttpServerService",
                      b"READ_SMS", b"SEND_SMS", b"cancelNotification"):
        assert forbidden not in dex, forbidden

print(f"Independent notification relay APK verified: {APK.name}")
