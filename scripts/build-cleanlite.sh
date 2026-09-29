#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"
if [[ -f scripts/env.local.sh ]]; then source scripts/env.local.sh; fi
: "${ANDROID_HOME:?Android SDK required}"
export JAVA_HOME="${PLAY_JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$PATH"
[[ -f signing.properties ]] || { echo 'Existing signing.properties is required' >&2; exit 1; }
bash ./play-lite/gradlew -p play-lite :cleanlite:assembleRelease :cleanlite:lintRelease --no-daemon --console=plain
python3 scripts/check-cleanlite-apk.py
mkdir -p dist/cleanlite
cp build/cleanlite/outputs/apk/release/NotifyRelay_1_release.apk dist/cleanlite/
(
    cd dist/cleanlite
    sha256sum NotifyRelay_1_release.apk > SHA256SUMS
)
