
#include "Renderer.h"


static Renderer *g_renderer = nullptr;

extern "C" {
JNIEXPORT void JNICALL glInit(JNIEnv *env, jobject obj, jobject surface);
JNIEXPORT void JNICALL glDraw(JNIEnv *env, jobject obj, jbyteArray yuvData);
JNIEXPORT void JNICALL glRelease(JNIEnv *env, jobject obj);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("ai/juyou/hookhelper/GLRenderer");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    JNINativeMethod methods[] = {
            {"glInit",    "(Landroid/view/Surface;)V", reinterpret_cast<void *>(glInit)},
            {"glDraw",    "([B)V",                     reinterpret_cast<void *>(glDraw)},
            {"glRelease", "()V",                       reinterpret_cast<void *>(glRelease)}
    };

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL glInit(JNIEnv *env, jobject, jobject surface) {
    if (g_renderer) {
        delete g_renderer;
        g_renderer = nullptr;
    }

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    if (!window) {
        ALOGE("Could not get native window from surface");
        return;
    }

    g_renderer = new Renderer();
    g_renderer->init(window);
}

JNIEXPORT void JNICALL glDraw(JNIEnv *env, jobject, jbyteArray yuvData) {
    jbyte *yuvDataPtr = env->GetByteArrayElements(yuvData, nullptr);
    if (g_renderer) {
        g_renderer->draw(reinterpret_cast<uint8_t*>(yuvDataPtr));
    }
    env->ReleaseByteArrayElements(yuvData, yuvDataPtr, 0);
}

JNIEXPORT void JNICALL glRelease(JNIEnv *, jobject) {
    if (g_renderer) {
        delete g_renderer;
        g_renderer = nullptr;
    }
}