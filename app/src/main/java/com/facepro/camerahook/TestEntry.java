package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;

import ai.juyou.hookhelper.ActivityHook;
import ai.juyou.hookhelper.HttpServer;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TestEntry {
    private static final String TAG = "CameraHook";
    HttpServer httpServer = null;
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {

        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                try {
                    httpServer = new HttpServer(activity);
                }
                catch (Exception e){
                    Log.e(TAG, "httpServer", e);
                }
            }
        });
    }
}
