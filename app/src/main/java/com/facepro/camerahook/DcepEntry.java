package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.TextView;

import ai.juyou.hookhelper.HookHelper;
import ai.juyou.hookhelper.ViewTree;
import ai.juyou.hookhelper.WaitCallback;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DcepEntry {

    private static final String TAG = "CameraHook";

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {

        XposedHelpers.findAndHookConstructor(Application.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                //Context context = (Context) param.args[0];
                //performLoadPackage(context, lpParam);
                Application app = (Application)param.thisObject;
                String processName = Application.getProcessName();
                String className = app.getClass().getName();

                //Log.d(TAG, "className: " + className +"\nprocessName: " + processName);

                if(className.equals("com.alipay.mobile.framework.quinoxless.QuinoxlessApplication")
                        && processName.equals("cn.gov.pbc.dcep")) {
                    performLoadPackage(lpParam);
                }
            }
        });
    }

    private boolean isFirst = true;

    DcepActivityHook dcepActivityHook;

    private void performLoadPackage(XC_LoadPackage.LoadPackageParam lpParam)
    {
        dcepActivityHook = new DcepActivityHook();
        dcepActivityHook.hook(lpParam);





    }
}
