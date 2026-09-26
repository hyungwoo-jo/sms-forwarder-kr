#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_dir"
if [[ -f scripts/env.local.sh ]]; then source scripts/env.local.sh; fi
: "${JAVA_HOME:?Set JAVA_HOME or create scripts/env.local.sh}"
: "${ANDROID_HOME:?Set ANDROID_HOME or create scripts/env.local.sh}"
[[ -x "$JAVA_HOME/bin/java" ]] || { echo 'JDK is missing' >&2; exit 1; }
[[ -f "$ANDROID_HOME/platforms/android-33/android.jar" ]] || { echo 'Android API 33 is missing' >&2; exit 1; }
[[ -f signing.properties ]] || { echo 'Run python3 scripts/init-signing.py first' >&2; exit 1; }
python3 scripts/check_localization.py
python3 scripts/check_privacy.py
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon --console=plain -PisNeedClean=false -PisNeedPackage=false
# The release resource merger can retain invalid generated source paths
# after resource shrinking. Regenerate this task's outputs without a full clean.
bash ./gradlew :app:mergeReleaseResources --rerun-tasks --no-daemon --console=plain -PisNeedClean=false -PisNeedPackage=true -PexcludeFrpclib=false
bash ./gradlew :app:assembleRelease :app:lintRelease --no-daemon --console=plain --stacktrace -PisNeedClean=false -PisNeedPackage=true -PexcludeFrpclib=false
python3 scripts/collect-artifacts.py
