//
// Created by Zhao YongKui on 4/6/23.
//

#ifndef CAMERAHOOK_RENDERER_H
#define CAMERAHOOK_RENDERER_H

#include <jni.h>
#include <string>
#include <android/native_window.h>

extern "C"{
JNIEXPORT void JNICALL init(JNIEnv *env, jobject obj, jobject surface);
JNIEXPORT void JNICALL resize(JNIEnv *env, jobject obj, jint width, jint height);
JNIEXPORT void JNICALL render(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL release(JNIEnv *env, jobject obj);
};

class Renderer {
public:
    Renderer();
    ~Renderer();

    void init(ANativeWindow *window);
    void resize(int width, int height);
    void render();
private:
    ANativeWindow *mWindow;
    int mWidth;
    int mHeight;
};



#endif //CAMERAHOOK_RENDERER_H
