#include "NovaNative.h"
#include <cassert>
#include <cmath>
#include <cstring>

namespace {
double rotation = 0.0;
double speed = 45.0;
bool getNumber(void*, const char* key, double* out) {
    if (!key || !out) return false;
    if (std::strcmp(key, "degreesPerSecond") == 0) { *out = speed; return true; }
    if (std::strcmp(key, "rotationY") == 0) { *out = rotation; return true; }
    return false;
}
void setNumber(void*, const char* key, double value) { if (key && std::strcmp(key, "rotationY") == 0) rotation = value; }
}

int main() {
    assert(Nova_GetAbiVersion() == 1);
    NovaComponentV1 component{};
    assert(Nova_CreateComponent(&component));
    assert(component.abiVersion == 1 && component.state != nullptr);
    NovaApiV1 api{1, nullptr, getNumber, setNumber};
    NovaObjectApiV1 object{nullptr};
    component.onStart(component.state, &api, &object);
    NovaFrameV1 firstFrame{1.0f, 1.0, 1};
    component.onUpdate(component.state, &api, &object, &firstFrame);
    assert(std::abs(rotation - 45.0) < 0.001);
    NovaFrameV1 secondFrame{0.5f, 1.5, 2};
    component.onUpdate(component.state, &api, &object, &secondFrame);
    assert(std::abs(rotation - 67.5) < 0.001);
    component.onDestroy(component.state, &api, &object);

    NovaVec3 moved = nova::moveTowards({0, 0, 0}, {3, 0, 4}, 2.0f);
    assert(std::abs(moved.x - 1.2f) < 0.001f && std::abs(moved.z - 1.6f) < 0.001f);
    return 0;
}
