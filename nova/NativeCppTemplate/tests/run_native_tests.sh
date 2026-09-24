#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD="$ROOT/build/host-tests"
cmake -S "$ROOT" -B "$BUILD" -DNOVA_BUILD_TESTS=ON
cmake --build "$BUILD" --parallel
ctest --test-dir "$BUILD" --output-on-failure
if [[ -n "${NOVA_SO:-}" ]]; then
  "$ROOT/tests/jni_smoke_test.sh" "$NOVA_SO"
fi
