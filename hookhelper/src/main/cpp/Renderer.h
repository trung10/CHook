

#ifndef PLAYER_RENDERER_H
#define PLAYER_RENDERER_H
#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <thread>

#define DEBUG 1

#define LOG_TAG "GLES3JNI"
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#if DEBUG
#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#else
#define ALOGV(...)
#endif

extern bool checkGlError(const char* funcName);
extern GLuint createShader(GLenum shaderType, const char* src);
extern GLuint createProgram(const char* vtxSrc, const char* fragSrc);

class Renderer {
public:
    Renderer();
    ~Renderer();


    bool init(ANativeWindow* window);
    void draw(uint8_t* yuvData);
private:
    ANativeWindow * mWindow;
    EGLDisplay mDisplay;
    EGLSurface mSurface;
    EGLContext mContext;
    GLuint mProgram;
    GLuint mTextures[3] = {0};
    int mVideoWidth;
    int mVideoHeight;
    int mScreenWidth, mScreenHeight;
};

#endif //PLAYER_RENDERER_H
