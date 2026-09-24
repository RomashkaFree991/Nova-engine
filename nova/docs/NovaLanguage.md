# NovaScript и native-совместимость

## Что работает сейчас

`.ns` исполняется встроенным интерпретатором `NovaScriptRuntime`: классы, поля, `OnStart`/`OnUpdate`, блоки, `if/else`, `while`, `for`, `foreach`-синтаксис, `break`/`continue`/`return`, `var`/`int`/`float`/`bool`/`string`, арифметика и сравнения, `vec3`, логирование, время, базовая математика, поиск объектов сцены и операции `Move`/`Rotate`/`SetColor`/`Destroy`.

`.cpp` поддерживается как native-модуль через CMake и стабильный C ABI v1. `.so` загружается по ABI из `native/<abi>/`, проверяет `Nova_GetAbiVersion`, создаёт компонент и вызывает `onStart`/`onUpdate`/`onDestroy`. JNI bridge связывает эти callbacks с `NativeComponent.java`.

## Контракт C++ компонента

```cpp
#include "NovaNative.h"
extern "C" NOVA_EXPORT bool Nova_CreateComponent(NovaComponentV1* out);
```

В `NovaApiV1` доступны чтение/запись числовых свойств объекта и логирование. `NovaFrameV1` передаёт `deltaTime`, elapsed time и номер кадра. `NovaVec3`, `NovaTransformV1` и `nova::moveTowards` дают минимальный базис для 2D/3D механик; рендерер сам остаётся владельцем OpenGL-состояния.

## C#

Полный C# runtime и CLR/AOT-компилятор в исходном архиве отсутствуют. Поэтому `.cs` сейчас является **планируемым source-compatibility слоем**, а не исполняемым форматом. Для безопасной реализации нужен выбранный runtime (например, IL2CPP/Mono/.NET for Android), сборка managed assembly и явная sandbox-модель. До этого C#-код нельзя автоматически считать совместимым с `.ns` или загружать как `.so`.

Рекомендуемый путь: ограниченный API-слой `Nova.*`, затем генератор bindings `.cs -> Nova C ABI` для whitelisted компонентов. Не следует загружать произвольный `.so` или managed assembly без проверки ABI, архитектуры и происхождения файла.

## UI, 2D и 3D

В текущем архиве уже есть Android UI редактора, GLES2 scene view, OBJ/glTF/GLB импорт и управление сценой. Но в заявленном runtime пока нет полноценного UI toolkit для игр, физики, аудио, анимации, ECS, частиц или общего managed runtime. Native ABI расширяет механику, но не создаёт эти подсистемы автоматически; их нужно добавлять отдельными API и тестами.
