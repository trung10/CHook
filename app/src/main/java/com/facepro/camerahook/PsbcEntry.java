package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PsbcEntry {
    private static final String TAG = "CameraHook";
    private static Camera.PreviewCallback mPreviewCallback;
    private static Camera.PreviewCallback mPreviewCallbackWithBuffer;
    private static VideoDecoder mVideoDecoder = null;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        if(lpParam.packageName.equals("com.yitong.mbank.psbc")){


            XposedHelpers.findAndHookConstructor(Application.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    //Context context = (Context) param.args[0];
                    //performLoadPackage(context, lpParam);
                    Application app = (Application)param.thisObject;
                    String processName = Application.getProcessName();
                    String className = app.getClass().getName();

                    //Log.d(TAG, "className: " + className +"\nprocessName: " + processName);

                    if(className.equals("com.yitong.mbank.psbc.YouBankApplication")
                            && processName.equals("com.yitong.mbank.psbc")) {

                        performLoadPackage(lpParam);

//                        XposedBridge.hookAllConstructors(PathClassLoader.class, new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                Object[] args = param.args;
//                                for(Object arg : args)
//                                {
//                                    //Log.d(TAG, "PathClassLoader arg: " + arg);
//                                }
//                            }
//                        });
//                        XposedBridge.hookAllConstructors(DexClassLoader.class, new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                Object[] args = param.args;
//                                for(Object arg : args)
//                                {
//                                    //Log.d(TAG, "DexClassLoader arg: " + arg);
//                                }
//                            }
//                        });
                    }
                }
            });
        }
    }


    private void performLoadPackage1(XC_LoadPackage.LoadPackageParam lpParam)
    {

    }


    private void performLoadPackage(XC_LoadPackage.LoadPackageParam lpParam)
    {
        //hookCamera(lpParam);
        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                //遍历activity的子view
                HookHelper.checkView(activity, false);
                //HookHelper.clickView(activity);

            }
        });
    }

    private void hookCamera(XC_LoadPackage.LoadPackageParam lpparam)
    {
        XposedHelpers.findAndHookMethod(Camera.class, "startPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "startPreview");
                Camera camera = (Camera)param.thisObject;
                Camera.Size size = camera.getParameters().getPreviewSize();
                Log.d(TAG, "startPreview: " + size.width + "x" + size.height);
            }
        });

        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewSurface", Surface.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "setPreviewSurface");
                for(Object args : param.args){
                    Log.d(TAG, "setPreviewSurface: " + args);
                }
            }
        });

//        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewTexture",SurfaceTexture.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Log.d(TAG, "setPreviewTexture");`
//            }
//        });

        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewCallback", Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "setPreviewCallback");
                for(Object args : param.args){
                    Log.d(TAG, "setPreviewCallback: " + args);
                }
                mPreviewCallback = (Camera.PreviewCallback)param.args[0];
            }
        });

        XposedHelpers.findAndHookMethod(Camera.class, "setPreviewCallbackWithBuffer",Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "setPreviewCallbackWithBuffer");
                for(Object args : param.args){
                    Log.d(TAG, "setPreviewCallbackWithBuffer: " + args);
                }
                mPreviewCallbackWithBuffer = (Camera.PreviewCallback)param.args[0];
                //param.args[0] = null;
            }
        });
    }
}
