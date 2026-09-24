#include "NovaNative.h"
#include <jni.h>
#include <new>
#include <string>
#include <cstring>

namespace {
struct NativeHandle {
    NovaComponentV1 component{};
    NovaApiV1 api{};
    NovaObjectApiV1 object{};
    double rotationY = 0.0;
    double degreesPerSecond = 45.0;
    char targetName[256]{};
};
void logMessage(int, const char*) {}
bool getNumber(void* raw, const char* key, double* out) {
    if (!raw || !key || !out) return false;
    auto* h = static_cast<NativeHandle*>(raw);
    if (std::string(key) == "rotationY") { *out = h->rotationY; return true; }
    if (std::string(key) == "degreesPerSecond") { *out = h->degreesPerSecond; return true; }
    return false;
}
void setNumber(void* raw, const char* key, double value) {
    if (!raw || !key) return;
    auto* h = static_cast<NativeHandle*>(raw);
    if (std::string(key) == "rotationY") h->rotationY = value;
    else if (std::string(key) == "degreesPerSecond") h->degreesPerSecond = value;
}
NativeHandle* from(jlong value) { return reinterpret_cast<NativeHandle*>(static_cast<std::uintptr_t>(value)); }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_nova_engine_NativeComponent_nativeCreate(JNIEnv* env, jclass, jstring targetName) {
    auto* h = new (std::nothrow) NativeHandle();
    if (!h) return 0;
    h->api = {1, logMessage, getNumber, setNumber};
    h->object.userData = h;
    if (targetName) { const char* chars = env->GetStringUTFChars(targetName, nullptr); if (chars) { std::strncpy(h->targetName, chars, sizeof(h->targetName)-1); env->ReleaseStringUTFChars(targetName, chars); } }
    if (Nova_GetAbiVersion() != 1 || !Nova_CreateComponent(&h->component) || h->component.abiVersion != 1) {
        delete h;
        return 0;
    }
    if (h->component.onStart) h->component.onStart(h->component.state, &h->api, &h->object);
    return static_cast<jlong>(reinterpret_cast<std::uintptr_t>(h));
}
extern "C" JNIEXPORT jdouble JNICALL
Java_com_nova_engine_NativeComponent_nativeUpdate(JNIEnv*, jclass, jlong value, jfloat dt, jdouble elapsed, jlong frame) {
    NativeHandle* h = from(value);
    if (!h) return 0.0;
    if (h->component.onUpdate) { NovaFrameV1 f{dt, elapsed, static_cast<std::uint64_t>(frame)}; h->component.onUpdate(h->component.state, &h->api, &h->object, &f); }
    return static_cast<jdouble>(h->rotationY);
}
extern "C" JNIEXPORT void JNICALL
Java_com_nova_engine_NativeComponent_nativeDestroy(JNIEnv*, jclass, jlong value) {
    NativeHandle* h = from(value);
    if (!h) return;
    if (h->component.onDestroy) h->component.onDestroy(h->component.state, &h->api, &h->object);
    delete h;
}
