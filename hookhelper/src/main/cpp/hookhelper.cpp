#include "Renderer.h"


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("ai/juyou/hookhelper/MyRenderer");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    static const JNINativeMethod methods[] = {
            {"init",    "(Landroid/view/Surface;)V",  reinterpret_cast<void *>(&init)},
            {"resize",  "(II)V",                      reinterpret_cast<void *>(&resize)},
            {"render",  "()V",                        reinterpret_cast<void *>(&render)},
            {"release", "()V",                        reinterpret_cast<void *>(&release)},
    };

    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
