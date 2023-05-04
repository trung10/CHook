package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

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


    private void performLoadPackage(XC_LoadPackage.LoadPackageParam lpParam)
    {
        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                HookHelper.checkView(activity,false);

            }
        });
    }
}
