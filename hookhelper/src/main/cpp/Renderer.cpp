
#include "Renderer.h"


bool checkGlError(const char* funcName) {
    GLenum err = glGetError();
    if (err != GL_NO_ERROR) {
        ALOGE("GL error after %s(): 0x%08x\n", funcName, err);
        return true;
    }
    return false;
}

GLuint createShader(GLenum shaderType, const char* src) {
    GLuint shader = glCreateShader(shaderType);
    if (!shader) {
        checkGlError("glCreateShader");
        return 0;
    }
    glShaderSource(shader, 1, &src, nullptr);

    GLint compiled = GL_FALSE;
    glCompileShader(shader);

    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLogLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen > 0) {
            auto* infoLog = (GLchar*)malloc(infoLogLen);
            if (infoLog) {
                glGetShaderInfoLog(shader, infoLogLen, nullptr, infoLog);
                ALOGE("Could not compile %s shader:\n%s\n",
                      shaderType == GL_VERTEX_SHADER ? "vertex" : "fragment",
                      infoLog);
                free(infoLog);
            }
        }
        glDeleteShader(shader);
        return 0;
    }

    return shader;
}

GLuint createProgram(const char* vtxSrc, const char* fragSrc) {
    GLuint vtxShader = 0;
    GLuint fragShader = 0;
    GLuint program = 0;
    GLint linked = GL_FALSE;

    vtxShader = createShader(GL_VERTEX_SHADER, vtxSrc);
    if (!vtxShader)
        goto exit;

    fragShader = createShader(GL_FRAGMENT_SHADER, fragSrc);
    if (!fragShader)
        goto exit;

    program = glCreateProgram();
    if (!program) {
        checkGlError("glCreateProgram");
        goto exit;
    }
    glAttachShader(program, vtxShader);
    glAttachShader(program, fragShader);

    glLinkProgram(program);
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        ALOGE("Could not link program");
        GLint infoLogLen = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen) {
            auto* infoLog = (GLchar*)malloc(infoLogLen);
            if (infoLog) {
                glGetProgramInfoLog(program, infoLogLen, nullptr, infoLog);
                ALOGE("Could not link program:\n%s\n", infoLog);
                free(infoLog);
            }
        }
        glDeleteProgram(program);
        program = 0;
    }

    exit:
    glDeleteShader(vtxShader);
    glDeleteShader(fragShader);
    return program;
}

static void printGlString(const char* name, GLenum s) {
    const char* v = (const char*)glGetString(s);
    ALOGV("GL %s: %s\n", name, v);
}

// ----------------------------------------------------------------------------

Renderer::Renderer() {
}

Renderer::~Renderer() {
    glDeleteProgram(mProgram);
    glDeleteTextures(3, mTextures);
    eglDestroyContext(mDisplay, mContext);
    eglDestroySurface(mDisplay, mSurface);
    eglTerminate(mDisplay);
    ANativeWindow_release(mWindow);
}


#define GET_STR(x) #x
static const char *VERTEX_SHADER = GET_STR(
        attribute vec4 aPosition;
        attribute vec2 aTextCoord;
        //uniform float uAspectRatio;
        varying vec2 vTextCoord;
        void main() {
            vTextCoord = aTextCoord;
            //vec4 scaledPosition = aPosition * vec4(uAspectRatio, 1.0, 1.0, 1.0);
            //gl_Position = scaledPosition;
            gl_Position = aPosition;
        }
);

static const char *FRAGMENT_SHADER = GET_STR(
        precision mediump float;

        varying vec2 vTextCoord;

        uniform sampler2D yTexture;
        uniform sampler2D uTexture;
        uniform sampler2D vTexture;
        void main() {
            float y = texture2D(yTexture, vTextCoord).r;
            float u = texture2D(uTexture, vTextCoord).r - 0.5;
            float v = texture2D(vTexture, vTextCoord).r - 0.5;
            float r = y + 1.13983 * v;
            float g = y - 0.39465 * u - 0.5806 * v;
            float b = y + 2.03211 * u;
            gl_FragColor = vec4(r, g, b, 1.0);
        }
);


static GLfloat vertex_coords[] = {
        -1.0f, -1.0f, 0.0f,  // 左下角
        1.0f, -1.0f, 0.0f,   // 右下角
        -1.0f, 1.0f, 0.0f,   // 左上角
        1.0f, 1.0f, 0.0f     // 右上角
};


static GLfloat texture_coords[] = {
        0.0f, 1.0f,  // 左下角
        1.0f, 1.0f,  // 右下角
        0.0f, 0.0f,  // 左上角
        1.0f, 0.0f   // 右上角
};

bool Renderer::init(ANativeWindow *window) {
    mScreenWidth = ANativeWindow_getWidth(window);
    mScreenHeight = ANativeWindow_getHeight(window);

    mDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint major, minor;
    eglInitialize(mDisplay, &major, &minor);

    EGLConfig config;
    EGLint numConfigs;
    EGLint attribList[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_DEPTH_SIZE, 0,
            EGL_NONE
    };

    eglChooseConfig(mDisplay, attribList, &config, 1, &numConfigs);

    mSurface = eglCreateWindowSurface(mDisplay, config, window, nullptr);
    EGLint contextAttribList[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
    };
    mContext = eglCreateContext(mDisplay, config, EGL_NO_CONTEXT, contextAttribList);
    eglMakeCurrent(mDisplay, mSurface, mSurface, mContext);

    mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    if (!mProgram)
        return false;

    glUseProgram(mProgram);

    GLint position_loc = glGetAttribLocation(mProgram, "aPosition");
    GLint texcoord_loc = glGetAttribLocation(mProgram, "aTextCoord");

    glVertexAttribPointer(position_loc, 3, GL_FLOAT, GL_FALSE, 0, vertex_coords);
    glVertexAttribPointer(texcoord_loc, 2, GL_FLOAT, GL_FALSE, 0, texture_coords);
    glEnableVertexAttribArray(texcoord_loc);
    glEnableVertexAttribArray(position_loc);

    glGenTextures(3, mTextures);

    glBindTexture(GL_TEXTURE_2D, mTextures[0]);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 mVideoWidth,
                 mVideoHeight,
                 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 NULL
    );

    glBindTexture(GL_TEXTURE_2D, mTextures[1]);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 mVideoWidth/2,
                 mVideoHeight/2,
                 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 NULL
    );

    glBindTexture(GL_TEXTURE_2D, mTextures[2]);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_LUMINANCE,
                 mVideoWidth/2,
                 mVideoHeight/2,
                 0,
                 GL_LUMINANCE,
                 GL_UNSIGNED_BYTE,
                 NULL
    );


    float videoAspectRatio = (float) mVideoWidth / mVideoHeight;
    float screenAspectRatio = (float) mScreenWidth / mScreenHeight;

    // 计算需要设置的视口大小
    int viewportWidth, viewportHeight;
    if (videoAspectRatio > screenAspectRatio) {
        // 视频宽高比较宽，需要调整视口高度
        viewportWidth = mScreenWidth;
        viewportHeight = mScreenWidth / videoAspectRatio;
    } else {
        // 视频宽高比较窄，需要调整视口宽度
        viewportWidth = mScreenHeight * videoAspectRatio;
        viewportHeight = mScreenHeight;
    }

    glViewport(0, mScreenHeight-viewportHeight, viewportWidth, viewportHeight);

    return true;
}

void Renderer::draw(uint8_t* yuvData) {
    glUseProgram(mProgram);

    glActiveTexture(GL_TEXTURE0);
    glUniform1i(glGetUniformLocation(mProgram, "yTexture"), 0);
    glBindTexture(GL_TEXTURE_2D, mTextures[0]);
    glTexSubImage2D(GL_TEXTURE_2D, 0,
                    0, 0,
                    mVideoWidth, mVideoHeight,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE,
                    yuvData);


    glActiveTexture(GL_TEXTURE1);
    glUniform1i(glGetUniformLocation(mProgram, "uTexture"), 1);
    glBindTexture(GL_TEXTURE_2D, mTextures[1]);
    glTexSubImage2D(GL_TEXTURE_2D, 0,
                    0, 0,
                    mVideoWidth / 2, mVideoHeight / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE,
                    yuvData + mVideoWidth * mVideoHeight * 5 / 4);


    glActiveTexture(GL_TEXTURE2);
    glUniform1i(glGetUniformLocation(mProgram, "vTexture"), 2);
    glBindTexture(GL_TEXTURE_2D, mTextures[2]);
    glTexSubImage2D(GL_TEXTURE_2D, 0,
                    0, 0,
                    mVideoWidth / 2, mVideoHeight / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE,
                    yuvData + mVideoWidth * mVideoHeight);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    eglSwapBuffers(mDisplay, mSurface);
}