

#include <jni.h>
#include <string>

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

//    jclass clazz = env->FindClass("ai/juyou/deepfake/GLRenderer");
//    if (clazz == nullptr) {
//        return JNI_ERR;
//    }
//
//    JNINativeMethod methods[] = {
//            {"glInit",    "(Landroid/view/Surface;)V", reinterpret_cast<void *>(glInit)},
//            {"glDraw",    "([B)V",                     reinterpret_cast<void *>(glDraw)},
//            {"glRelease", "()V",                       reinterpret_cast<void *>(glRelease)}
//    };

//    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0) {
//        return JNI_ERR;
//    }

    return JNI_VERSION_1_6;
}
