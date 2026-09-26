#!/usr/bin/env python3
"""Create a local personal release key once; never print credentials or overwrite keys."""
import os
import secrets
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT.parent / ".smsforwarder-local/signing"
KEY = DIRECTORY / "smsforwarder-personal.p12"
PROPERTIES = ROOT / "signing.properties"
if KEY.exists() and PROPERTIES.exists():
    print("Existing personal signing key retained")
    raise SystemExit(0)
if KEY.exists() or PROPERTIES.exists():
    raise SystemExit("Partial signing setup exists; retain it and repair manually")
DIRECTORY.mkdir(parents=True, exist_ok=True)
DIRECTORY.chmod(0o700)
password_file = DIRECTORY / "password.txt"
if password_file.exists():
    raise SystemExit("Existing signing password file retained; repair setup manually")
password = secrets.token_urlsafe(36)
fd = os.open(password_file, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
with os.fdopen(fd, "w") as stream:
    stream.write(password + "\n")
keytool = str(Path(os.environ["JAVA_HOME"]) / "bin/keytool")
subprocess.run([keytool, "-genkeypair", "-alias", "smsforwarder-personal", "-keyalg", "RSA", "-keysize", "3072", "-validity", "10000", "-dname", "CN=SmsForwarder Personal", "-storetype", "PKCS12", "-keystore", str(KEY), "-storepass:file", str(password_file), "-keypass:file", str(password_file), "-noprompt"], check=True)
KEY.chmod(0o600)
fd = os.open(PROPERTIES, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
with os.fdopen(fd, "w") as stream:
    stream.write(f"storeFile={KEY}\nstorePassword={password}\nkeyAlias=smsforwarder-personal\nkeyPassword={password}\n")
print("Personal signing key created; private files remain local")
