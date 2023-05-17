package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import ai.juyou.hookhelper.HookHelper;
import ai.juyou.hookhelper.HttpServer;
import ai.juyou.hookhelper.ViewTree;
import ai.juyou.hookhelper.WaitCallback;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PsbcEntry {
    private static final String TAG = "CameraHook";

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
                        //HOOK主程序
                        hookMain(lpParam);
                        //hookTest(lpParam);
                    }
                }
            });
        }
    }
    private void hookTest(XC_LoadPackage.LoadPackageParam lpParam){
        Log.d(TAG, "dataDir: " + lpParam.appInfo.dataDir);
    }
    private HookCamera hookCamera = null;
    private PsbcActivityHook psbcActivityHook = null;

    private void hookMain(XC_LoadPackage.LoadPackageParam lpParam)
    {
        hookCamera = new HookCamera(lpParam);
        hookCamera.hook1();

        psbcActivityHook = new PsbcActivityHook();
        psbcActivityHook.hook(lpParam);
    }
}
