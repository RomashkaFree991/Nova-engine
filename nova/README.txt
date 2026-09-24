NOVA mobile editor source bundle

Copy app/src/main/java/com/nova/engine/*.java into the same package folder in CodeAssist and replace app/src/main/AndroidManifest.xml. Keep package/namespace/applicationId com.nova.engine and minSdk >= 24. No Maven dependencies. This archive contains source files, not a prebuilt APK or a standalone Gradle project.

Includes dark Russian landscape menu, project creation, provider chat, compact workspace controls, scene view, code editor, project file browser, and a native C++ component template. Projects are stored in Android app external files/projects/<name>/.

NovaScript interpreter scope
The interpreter is an initial practical subset, not a complete implementation of every planned NovaScript feature. It supports class name matching the .ns filename, fields, OnStart/OnUpdate, blocks, if/else, while, classic for, foreach syntax, break/continue/return, var/int/float/bool/string declarations, literals, assignments, comparisons, boolean and arithmetic operators, vec3, Log.Print/Warn/Error, Time.Delta, basic Math (Abs/Sqrt/Floor/Round/Min/Max/Clamp), Scene.Find, and selected NovaObject Move/Rotate/SetColor/Destroy/position/visible operations. Errors show file and line. The runtime polls changed .ns files, reloads, and reruns OnStart. A 10,000-step budget per callback guards against accidental endless loops.

Not implemented yet: List/Dict/array runtime values, lambdas, interpolated strings, switch, do/while, try/catch, coroutines, general method calls, full type checking, physics, UI, audio, and every API in the long-term language spec. Foreach syntax needs an iterable value, but collection literals/APIs are not implemented yet.

Model import
OBJ supports positions and faces, fan-triangulated. glTF/GLB supports the first mesh and first TRIANGLES primitive, POSITION, optional TEXCOORD_0, indices, and one PBR base-color texture from a data URI or embedded GLB bufferView. GLB 2.0 supported. The loader does not apply node transforms/hierarchies, additional primitives/materials, animations, skins, normals, or extensions. Standalone .gltf works when its referenced buffers and images are embedded as data URIs; external sidecar resources must be available at their relative paths under assets/models. GLB is the reliable self-contained option. OBJ materials/MTL and textures are not loaded.

C++ NDK component template
The standalone NativeCppTemplate/ and generated native/ files in each new project contain a versioned C ABI component, CMakeLists.txt and lifecycle example. Build with Android NDK CMake, Android ABI arm64-v8a or armeabi-v7a, platform android-24+. Host syntax check uses C++17. IMPORTANT: this app does not yet dlopen the resulting .so or dispatch component callbacks, so the template is source/build-ready but not integrated at runtime. C++ is optional; NovaScript is still used for normal project logic. No server is needed for local scene, model import or script execution; remote AI requests require internet.

Suggested build command from NativeCppTemplate:
cmake -S . -B build -DCMAKE_TOOLCHAIN_FILE="$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake" -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-24 -DCMAKE_BUILD_TYPE=Release
cmake --build build

## Duplicate runButton compile fix and .so loader

WorkspaceActivity now declares `runButton` only once as an `IconButton`; the duplicate `Button runButton` was removed. The editor's project-files dialog can import a native `.so` into `native/<device ABI>/`, then `NativeLibraryManager` selects the supported ABI, `System.load`s the library, and `NativeComponent` drives the JNI lifecycle (`nativeCreate`, `nativeUpdate`, `nativeDestroy`) while the editor is open. Exiting the editor destroys the loaded components.

The C++ template now includes `src/NovaJniBridge.cpp` and builds both C ABI lifecycle functions and JNI exports. It was host-compiled and its exported symbols were checked. Actual Android NDK build and on-device loading still need to be tested in CodeAssist/Termux. The demo currently updates a native numeric property; it is not yet mapped onto a visible scene object's transform.


Native rotation wiring: when importing the NDK library, Nova assigns it to the first visible renderable scene object. The C++ component advances `rotationY` in degrees per second; JNI returns the updated angle each frame; Java applies it as an extra Y rotation in the GLES2 renderer and writes the final angle into scene.nscn on editor exit. This template-specific bridge rotates the visible object, while arbitrary third-party `.so` files must export the matching Nova JNI lifecycle symbols.


## Дополнения этой версии

В `NativeCppTemplate` добавлены стабильный C ABI v1, header-only типы `NovaVec3`/`NovaTransformV1` и механика `nova::moveTowards`, host CMake/CTest автотест lifecycle и сборщик Android ABI для `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`. JNI smoke-тест проверяет экспорт `Nova_GetAbiVersion`, `Nova_CreateComponent` и три lifecycle JNI-функции. Документация языка и совместимости находится в `docs/NovaLanguage.md`.

Проверено в sandbox: host native test `nova_native_lifecycle` — **passed**. Android NDK в sandbox отсутствует, поэтому Android `.so` должны быть собраны на машине с установленным NDK командой `NativeCppTemplate/build_all_abis.sh`. Архив не содержит Gradle-проекта: это исходники для помещения в существующий CodeAssist/Android проект, как указано выше.


## Исправления после проверки на устройстве

AI client теперь при HTTP 404 с недоступной моделью запрашивает `/models` у OpenAI-compatible провайдера и один раз повторяет запрос с первой доступной моделью. Если список моделей недоступен, показывается понятное сообщение вместо необработанного имени модели.

Импорт OBJ больше не парсит файл синхронно в UI-потоке. OBJ нормализуется к размеру камеры и имеет ограничение в 100000 треугольников; при ошибке renderer оставляет безопасный placeholder, поэтому крупный asset не должен блокировать повторное открытие проекта. Файловые панели показывают каталоги и только открываемые текстовые файлы; `.obj`, `.glb`, `.so`, изображения и прочие бинарные файлы исключены из редактора текста.

NovaScript получил базовые генераторы: `Scene.Spawn`, `Terrain.Create`, `Vehicle.Create`, `Player.Create`, `Camera.SetPosition`. Они создают редактируемые scene objects и базовые формы/масштабы в текущем GLES2 renderer. Это foundation для дальнейшего физического транспорта, terrain-сетки, камеры-follow и импорта художественных моделей, а не готовый photorealistic/physics runtime.


## Gameplay API

Добавлен `docs/NovaGameplay.md` с примерами. NovaScript теперь поддерживает `Vehicle.Create`, `Vehicle.Configure`, `Vehicle.Control`, `Terrain.Generate`, `Terrain.SetTexture`, `Tree.Create` и `Interaction.Chop`. Vehicle controller применяет ускорение, drag, тормоз, руление, ограничение скорости и ground clamp. Terrain получает детерминированную 16x16 heightmap и renderer строит triangle grid. Tree/Interaction реализуют здоровье дерева и падение за 1.2 секунды.

Это рабочий расширяемый arcade/foundation слой для Nova, а не полноценная физика rigid-body, wheel suspension или готовая AAA-анимационная система. Для production-игры ещё потребуются коллизии, физические колёса, input system, sprite/skeletal animation, audio, particles, navmesh и полноценный runtime renderer.
