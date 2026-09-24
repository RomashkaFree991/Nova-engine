#pragma once
#include <cstdint>

#if defined(__GNUC__)
#define NOVA_EXPORT __attribute__((visibility("default")))
#else
#define NOVA_EXPORT
#endif

extern "C" {

/** Stable plugin ABI. Keep v1 structs append-only and use Nova_GetAbiVersion(). */
struct NovaApiV1 {
    std::uint32_t abiVersion;
    void (*log)(int level, const char* message);
    bool (*getNumber)(void* object, const char* key, double* outValue);
    void (*setNumber)(void* object, const char* key, double value);
};
struct NovaObjectApiV1 { void* userData; };
struct NovaFrameV1 { float deltaTime; double elapsedTime; std::uint64_t frame; };
struct NovaComponentV1 {
    std::uint32_t abiVersion;
    void* state;
    void (*onStart)(void* state, const NovaApiV1* api, NovaObjectApiV1* object);
    void (*onUpdate)(void* state, const NovaApiV1* api, NovaObjectApiV1* object, const NovaFrameV1* frame);
    void (*onDestroy)(void* state, const NovaApiV1* api, NovaObjectApiV1* object);
};

/** Small engine-independent mechanics types for .cpp plugins. */
struct NovaVec3 { float x; float y; float z; };
struct NovaTransformV1 { NovaVec3 position; NovaVec3 rotationEuler; NovaVec3 scale; };

NOVA_EXPORT std::uint32_t Nova_GetAbiVersion();
NOVA_EXPORT bool Nova_CreateComponent(NovaComponentV1* outComponent);
}

#ifdef __cplusplus
/** Header-only helpers are usable from any C++17 gameplay module. */
namespace nova {
inline float clamp(float value, float low, float high) { return value < low ? low : (value > high ? high : value); }
inline NovaVec3 add(NovaVec3 a, NovaVec3 b) { return {a.x+b.x, a.y+b.y, a.z+b.z}; }
inline NovaVec3 scale(NovaVec3 a, float factor) { return {a.x*factor, a.y*factor, a.z*factor}; }
inline NovaVec3 moveTowards(NovaVec3 current, NovaVec3 target, float maxDistanceDelta) {
    const NovaVec3 d{target.x-current.x, target.y-current.y, target.z-current.z};
    const float length = __builtin_sqrtf(d.x*d.x+d.y*d.y+d.z*d.z);
    if (length <= maxDistanceDelta || length == 0.0f) return target;
    return add(current, scale(d, maxDistanceDelta/length));
}
}
#endif
