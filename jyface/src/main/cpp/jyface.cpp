#include <jni.h>
#include <string>

/**
 * 第一个测试函数
 */
void testFunc1(JNIEnv* env, jobject obj) {
    printf("This is a test function 1.\n");
}

/**
 * 第二个测试函数
 */
void testFunc2(JNIEnv* env, jobject obj, jstring str) {
    const char* utf = env->GetStringUTFChars(str, NULL);
    printf("This is a test function 2. String: %s\n", utf);
    env->ReleaseStringUTFChars(str, utf);
}


/**
 * 定义需要注册的函数数组
 */
static JNINativeMethod gMethods[] = {
        {"test1", "()V", (void*)testFunc1},
        {"test2", "(Ljava/lang/String;)V", (void*)testFunc2},
};

/**
 * 注册函数
 */
int registerNativeMethods(JNIEnv* env, const char* className,
                          const JNINativeMethod* gMethod, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethod, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/**
 * 初始化函数，注册函数
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (!registerNativeMethods(env, "ai/juyou/deepfake/AutoFitTextureView", gMethods, sizeof(gMethods)/sizeof(gMethods[0]))) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
