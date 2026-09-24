#!/usr/bin/env bash
set -euo pipefail
SO="${1:?usage: jni_smoke_test.sh /path/to/libNovaComponent.so}"
[[ -f "$SO" ]] || { echo "missing: $SO" >&2; exit 2; }
NM="${LLVM_NM:-nm}"
for symbol in Nova_GetAbiVersion Nova_CreateComponent Java_com_nova_engine_NativeComponent_nativeCreate Java_com_nova_engine_NativeComponent_nativeUpdate Java_com_nova_engine_NativeComponent_nativeDestroy; do
  "$NM" -D --defined-only "$SO" 2>/dev/null | grep -q "$symbol" || { echo "missing JNI/ABI export: $symbol" >&2; exit 1; }
done
echo "JNI smoke test passed: $SO"
