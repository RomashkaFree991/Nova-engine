#include "NovaNative.h"
#include <new>

namespace {
struct SpinState { double degreesPerSecond = 45.0; double angle = 0.0; };
void onStart(void* raw, const NovaApiV1* api, NovaObjectApiV1* object) {
    if (!raw) return;
    auto* state = static_cast<SpinState*>(raw);
    if (api && api->log) api->log(0, "Nova C++ component started");
    double speed = 0.0;
    if (api && api->getNumber && object && api->getNumber(object->userData, "degreesPerSecond", &speed))
        state->degreesPerSecond = speed;
}
void onUpdate(void* raw, const NovaApiV1* api, NovaObjectApiV1* object, const NovaFrameV1* frame) {
    if (!raw || !frame) return;
    auto* state = static_cast<SpinState*>(raw);
    state->angle += state->degreesPerSecond * frame->deltaTime;
    if (api && api->setNumber && object)
        api->setNumber(object->userData, "rotationY", state->angle);
}
void onDestroy(void* raw, const NovaApiV1*, NovaObjectApiV1*) {
    delete static_cast<SpinState*>(raw);
}
}

extern "C" NOVA_EXPORT std::uint32_t Nova_GetAbiVersion() { return 1; }
extern "C" NOVA_EXPORT bool Nova_CreateComponent(NovaComponentV1* out) {
    if (!out) return false;
    auto* state = new (std::nothrow) SpinState();
    if (!state) return false;
    out->abiVersion = 1;
    out->state = state;
    out->onStart = onStart;
    out->onUpdate = onUpdate;
    out->onDestroy = onDestroy;
    return true;
}
