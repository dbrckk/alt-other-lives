#!/usr/bin/env bash
set +e

adb logcat -c || true
gradle :app:connectedDebugAndroidTest --stacktrace --info
instrumentation_status=$?

baseline_status=0
if [[ "$instrumentation_status" -eq 0 ]]; then
  gradle :app:generateBaselineProfile --stacktrace --info
  baseline_status=$?
fi

mkdir -p app/build
adb logcat -d > app/build/instrumentation-logcat.txt 2>&1 || true

if [[ "$instrumentation_status" -ne 0 ]]; then
  exit "$instrumentation_status"
fi
exit "$baseline_status"
