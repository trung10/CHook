package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import ai.juyou.hookhelper.Utilities;
import ai.juyou.hookhelper.ViewTree;
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

    private void performLoadPackage(XC_LoadPackage.LoadPackageParam lpParam)
    {
        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

                if(param.thisObject.getClass().getName().equals("cn.gov.pbc.dcep.main.activity.MainActivity")) {
                    if (!isFirst) return;
                    isFirst = false;
                    try {

                        View v = Utilities.findChildView(decorView, "我的");
                        //Log.d(TAG, "v: " + v);
                        ViewGroup item =(ViewGroup)v.getParent().getParent();

                        //Log.d(TAG, "item: " + item);
                        int itemId = item.getId();
                        //Log.d(TAG, "itemId: " + itemId);
                        ViewTree viewTree = Utilities.getViewTree(decorView);
                        View nav = viewTree.getView(40);
                        XposedHelpers.callMethod(nav, "setSelectedItemId", itemId);

                        Utilities.waitFindChildView(decorView, "钱包总额", new Utilities.WaitCallback() {
                            @Override
                            public void callback(Object obj) {
                                TextView textView = (TextView) obj;
                                ViewTree viewTree = Utilities.getViewTree((ViewGroup)textView.getParent());
                                View w = viewTree.getView(5);
                                w.performClick();
                            }
                        });

                        //NavigationBarView nav = (NavigationBarView) viewTree.getView(40);
                        //nav.setSelectedItemId(itemId);
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Exception: " + e.getMessage(),e);
                    }

                }
                else if(param.thisObject.getClass().getName().equals("cn.gov.pbc.dcep.main.activity.WalletOverallActivity")){
                    try {
                        ViewTree viewTree = Utilities.getViewTree(decorView);
                        ViewGroup view = (ViewGroup)viewTree.getView(12);
                        Utilities.waitGetChildView(view, 0, new Utilities.WaitCallback() {
                            @Override
                            public void callback(Object obj) {
                                Log.d(TAG, "callback: " + obj);
                                XposedHelpers.callMethod(obj, "performClick");
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Exception: " + e.getMessage(),e);
                    }
                }
                else if(param.thisObject.getClass().getName().equals("com.alipay.mobile.nebulacore.ui.H5Activity")){
                    try {
                        Utilities.waitCall(1000, decorView, new Utilities.WaitCallback() {
                            @Override
                            public void callback(Object obj) {
                                ViewTree viewTree = Utilities.getViewTree(decorView);
                                Log.d(TAG, "viewTree: " + viewTree);
                                View view = viewTree.getView(20);

                                XposedHelpers.callMethod(view, "evaluateJavascript", "document.documentElement.outerHTML", new ValueCallback(){
                                    @Override
                                    public void onReceiveValue(Object value) {
                                        Log.d(TAG, "onReceiveValue: " + value);
                                    }
                                });

                                //HookHelper.showSuperClass(view.getClass());
                                //HookHelper.showDeclaredMethods(view.getClass());
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Exception: " + e.getMessage(),e);
                    }
                }
            }
        });




    }
}
