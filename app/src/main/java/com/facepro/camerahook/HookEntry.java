package com.facepro.camerahook;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import ai.juyou.hookhelper.HttpServer;
import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "CameraHook";


    DcepEntry dcepEntry;
    PsbcEntry psbcEntry;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: " + lpParam.packageName);
        if(lpParam.packageName.equals("com.yitong.mbank.psbc")) {
            psbcEntry = new PsbcEntry();
            psbcEntry.handleLoadPackage(lpParam);
        }
        else if(lpParam.packageName.equals("cn.gov.pbc.dcep")) {
            dcepEntry = new DcepEntry();
            dcepEntry.handleLoadPackage(lpParam);
        }
    }
}
