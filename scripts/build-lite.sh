#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"
if [[ -f scripts/env.local.sh ]]; then source scripts/env.local.sh; fi
: "${JAVA_HOME:?JDK required}"
: "${ANDROID_HOME:?Android SDK required}"
export JAVA_HOME="${PLAY_JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$PATH"
[[ -f signing.properties ]] || { echo 'Existing signing.properties is required' >&2; exit 1; }
bash ./play-lite/gradlew -p play-lite :lite:assembleRelease :lite:bundleRelease :lite:lintRelease --no-daemon --console=plain
python3 scripts/check-lite-apk.py
mkdir -p dist/lite
cp build/lite-play/outputs/apk/release/SmsKR_Notify_Lite_300066_release.apk dist/lite/
cp build/lite-play/outputs/bundle/release/lite-release.aab dist/lite/SmsKR_Notify_Lite_300066_release.aab
cp docs/README-WORKS-LITE-ko.md dist/lite/
(
    cd dist/lite
    sha256sum SmsKR_Notify_Lite_300066_release.apk SmsKR_Notify_Lite_300066_release.aab > SHA256SUMS
)
