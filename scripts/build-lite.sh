#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"
if [[ -f scripts/env.local.sh ]]; then source scripts/env.local.sh; fi
: "${JAVA_HOME:?JDK required}"
: "${ANDROID_HOME:?Android SDK required}"
[[ -f signing.properties ]] || { echo 'Existing signing.properties is required' >&2; exit 1; }
bash ./gradlew :lite:assembleRelease :lite:lintRelease --no-daemon --console=plain -PisNeedClean=false -PisNeedPackage=false
python3 scripts/check-lite-apk.py
mkdir -p dist/lite
cp build/lite/outputs/apk/release/SmsKR_Notify_Lite_300064_release.apk dist/lite/
cp docs/README-WORKS-LITE-ko.md dist/lite/
(
    cd dist/lite
    sha256sum SmsKR_Notify_Lite_300064_release.apk > SHA256SUMS
)
