#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
NDK="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
OUT="${1:-$ROOT/dist}"
if [[ -z "$NDK" || ! -f "$NDK/build/cmake/android.toolchain.cmake" ]]; then
  echo "ERROR: set ANDROID_NDK_HOME or ANDROID_NDK_ROOT to an installed Android NDK" >&2
  exit 2
fi
mkdir -p "$OUT"
for abi in arm64-v8a armeabi-v7a x86 x86_64; do
  build="$ROOT/build/$abi"
  cmake -S "$ROOT" -B "$build" -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE="$NDK/build/cmake/android.toolchain.cmake" \
    -DANDROID_ABI="$abi" -DANDROID_PLATFORM=android-24 -DCMAKE_BUILD_TYPE=Release
  cmake --build "$build" --parallel
  mkdir -p "$OUT/$abi"
  cp "$build/libNovaComponent.so" "$OUT/$abi/"
done
echo "Built NovaComponent for: arm64-v8a armeabi-v7a x86 x86_64"
