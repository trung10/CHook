package com.facepro.camerahook;

import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookCamera {
    private static final String TAG = "CameraHook";

    private Camera.PreviewCallback mOriginPreviewCallback;

    private Camera.PreviewCallback mHookedPreviewCallback;


    private boolean isSaveFile = false;

    public HookCamera(XC_LoadPackage.LoadPackageParam lpParam)
    {
        Log.d(TAG, "HookCamera");
        mHookedPreviewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.d(TAG, "onPreviewFrame: " + data.length);
//                String filePath = lpParam.appInfo.dataDir+"/files/preview.yuv";
//                if(!isSaveFile){
//                    isSaveFile = true;
//                    Log.d(TAG,""+filePath);
//                    try {
//                        FileOutputStream outputStream = new FileOutputStream(filePath, false);
//                        outputStream.write(data);
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                mOriginPreviewCallback.onPreviewFrame(data, camera);
            }
        };
    }
    private VideoDecoder mVideoDecoder = null;
    public void hook1()
    {
        mVideoDecoder = new VideoDecoder();
        mVideoDecoder.setCallback(new VideoDecoder.Callback() {
            @Override
            public void onFrame(byte[] data,Camera camera) {
                //Log.d(TAG, "onFrame: " + data.length);
                mOriginPreviewCallback.onPreviewFrame(data,camera);
            }
        });
        XposedHelpers.findAndHookMethod(Camera.class, "startPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "startPreview");
                Camera camera = (Camera)param.thisObject;
                Camera.Size size = camera.getParameters().getPreviewSize();
                Log.d(TAG, "startPreview: " + size.width + "x" + size.height);
                mVideoDecoder.start(size.width,size.height);
            }
        });

        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewSurface", Surface.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "setPreviewSurface");
                final Camera camera = (Camera)param.thisObject;
                for(Object args : param.args){
                    Log.d(TAG, "setPreviewSurface: " + args);
                }
                Surface surface = (Surface) param.args[0];
                mVideoDecoder.setSurface(surface);
                mVideoDecoder.setCamera(camera);
                param.args[0] = null;
            }
        });

        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewCallbackWithBuffer",Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "setPreviewCallbackWithBuffer");
                for(Object args : param.args){
                    Log.d(TAG, "setPreviewCallbackWithBuffer: " + args);
                }
                mOriginPreviewCallback = (Camera.PreviewCallback)param.args[0];
                param.args[0] = mHookedPreviewCallback;
            }
        });

        XposedHelpers.findAndHookMethod(Camera.class, "addCallbackBuffer", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                //Log.d(TAG, "addCallbackBuffer");
                for(Object args : param.args){
                    //Log.d(TAG, "addCallbackBuffer: " + args);
                }
                byte[] buffer = (byte[])param.args[0];
                mVideoDecoder.addCallbackBuffer(buffer);
            }
        });
//
//        XposedHelpers.findAndHookMethod(Camera.class, "addRawImageCallbackBuffer", byte[].class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Log.d(TAG, "addRawImageCallbackBuffer");
//                for(Object args : param.args){
//                    Log.d(TAG, "addRawImageCallbackBuffer: " + args);
//                }
//            }
//        });
//
//
//        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewTexture",SurfaceTexture.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Log.d(TAG, "setPreviewTexture");`
//            }
//        });
//        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewCallback", Camera.PreviewCallback.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Log.d(TAG, "setPreviewCallback");
//                for(Object args : param.args){
//                    Log.d(TAG, "setPreviewCallback: " + args);
//                }
//                mPreviewCallback = (Camera.PreviewCallback)param.args[0];
//            }
//        });


    }


    public void hookTest(XC_LoadPackage.LoadPackageParam lpparam)
    {
        Method[] methods = Camera.class.getDeclaredMethods();
        XC_MethodHook cb = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "hookTest: " + param.method.getName());
                if(param.method.getName().equals("setParameters")){
                    Camera.Parameters parameters = (Camera.Parameters)param.args[0];
                    //parameters.
                }
            }
        };
        for(Method method : methods){
            if (Modifier.isPublic(method.getModifiers())) {
                XposedBridge.hookMethod(method, cb);
            }
        }
    }
}
