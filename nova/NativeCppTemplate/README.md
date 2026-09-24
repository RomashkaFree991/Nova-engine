# Nova C++ NDK component template

Это минимальный production-oriented native SDK для Nova: стабильный **C ABI v1**, JNI lifecycle bridge и header-only механики `NovaVec3`/`moveTowards`.

## Сборка всех Android ABI

Нужен установленный Android NDK с CMake toolchain:

```sh
export ANDROID_NDK_HOME=/path/to/android-ndk
./build_all_abis.sh
```

Скрипт собирает `arm64-v8a`, `armeabi-v7a`, `x86` и `x86_64` под `android-24` и кладёт библиотеки в `dist/<abi>/libNovaComponent.so`. В текущем sandbox NDK не установлен, поэтому эти `.so` здесь не выдаются как будто они были собраны.

Для одной ABI:

```sh
cmake -S . -B build/arm64-v8a -G Ninja \
  -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" \
  -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-24 -DCMAKE_BUILD_TYPE=Release
cmake --build build/arm64-v8a
```

## Автотесты

Host-тесты не требуют Android SDK:

```sh
./tests/run_native_tests.sh
```

Для уже собранного Android `.so` проверяются обязательные C ABI и JNI exports:

```sh
./tests/jni_smoke_test.sh dist/arm64-v8a/libNovaComponent.so
```

Ожидаемые exports: `Nova_GetAbiVersion`, `Nova_CreateComponent`, `Java_com_nova_engine_NativeComponent_nativeCreate`, `nativeUpdate`, `nativeDestroy`.

## Подключение

Скопируйте библиотеку в проект Nova как `native/<device-abi>/libNovaComponent.so`. `NativeLibraryManager` выбирает ABI из `Build.SUPPORTED_ABIS`, `NativeComponent` вызывает lifecycle и передаёт вращение в сцену. Произвольные `.so` должны соблюдать тот же контракт; несовместимые ABI отклоняются.

`.ns` остаётся основным скриптовым runtime. `.cpp` компилируется в этот ABI, `.so` загружается как его бинарный результат. Полная исполняемая совместимость с `.cs` в исходном проекте отсутствует и требует отдельного Mono/.NET/IL2CPP runtime, bindings и sandbox; это описано в `../docs/NovaLanguage.md`.
