//
// Created by Zhao YongKui on 4/6/23.
//
#include "Renderer.h"

static Renderer* renderer = nullptr;

JNIEXPORT void JNICALL init(JNIEnv *env, jobject obj, jobject surface)
{
    if(renderer != nullptr){
        delete renderer;
        renderer = nullptr;
    }

    renderer = new Renderer();
}

JNIEXPORT void JNICALL resize(JNIEnv *env, jobject obj, jint width, jint height)
{
    if(renderer != nullptr){
        renderer->resize(width, height);
    }
}

JNIEXPORT void JNICALL render(JNIEnv *env, jobject obj)
{
    if(renderer != nullptr){
        renderer->render();
    }
}

JNIEXPORT void JNICALL release(JNIEnv *env, jobject obj)
{
    if(renderer != nullptr){
        delete renderer;
        renderer = nullptr;
    }
}

Renderer::Renderer() {

}

Renderer::~Renderer() {

}

void Renderer::init(ANativeWindow *window) {

}

void Renderer::resize(int width, int height) {

}

void Renderer::render() {

}