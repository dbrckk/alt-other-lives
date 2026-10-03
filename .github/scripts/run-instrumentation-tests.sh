#!/usr/bin/env bash
set +e

adb logcat -c || true
gradle :app:connectedDebugAndroidTest --stacktrace --info
status=$?

mkdir -p app/build
adb logcat -d > app/build/instrumentation-logcat.txt 2>&1 || true

exit "$status"
