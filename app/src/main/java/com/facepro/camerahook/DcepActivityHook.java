package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.TextView;

import ai.juyou.hookhelper.ActivityHook;
import ai.juyou.hookhelper.HookHelper;
import ai.juyou.hookhelper.ViewTree;
import ai.juyou.hookhelper.WaitCallback;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DcepActivityHook extends ActivityHook {
    private static final String TAG = "CameraHook";


    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpParam){
        addResume("cn.gov.pbc.dcep.main.activity.MainActivity", 1, hookMainActivityCallback);
        addResume("cn.gov.pbc.dcep.main.activity.WalletOverallActivity", 1, hookWalletOverallActivityCallback);
        addResume("com.alipay.mobile.nebulacore.ui.H5Activity", 1, hookH5ActivityCallback);
        super.hook(lpParam);
    }

    private final Callback hookMainActivityCallback=new Callback() {
        @Override
        public void onHook(Activity activity) {
            try {
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                View v = HookHelper.findChildView(decorView, "我的");
                //Log.d(TAG, "v: " + v);
                ViewGroup item =(ViewGroup)v.getParent().getParent();

                //Log.d(TAG, "item: " + item);
                int itemId = item.getId();
                //Log.d(TAG, "itemId: " + itemId);
                ViewTree viewTree = HookHelper.getViewTree(decorView);
                View nav = viewTree.getView(40);
                XposedHelpers.callMethod(nav, "setSelectedItemId", itemId);

                HookHelper.waitFindChildView(decorView, "钱包总额", new WaitCallback() {
                    @Override
                    public void callback(Object obj) {
                        TextView textView = (TextView) obj;
                        ViewTree viewTree = HookHelper.getViewTree((ViewGroup)textView.getParent());
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
    };

    private final Callback hookWalletOverallActivityCallback=new Callback() {
        @Override
        public void onHook(Activity activity) {
            try {
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                ViewTree viewTree = HookHelper.getViewTree(decorView);
                ViewGroup view = (ViewGroup)viewTree.getView(12);
                HookHelper.waitGetChildView(view, 0, new WaitCallback() {
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
    };

    private final Callback hookH5ActivityCallback=new Callback() {
        @Override
        public void onHook(Activity activity) {
            try {
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                HookHelper.waitCall(1000, decorView, new WaitCallback() {
                    @Override
                    public void callback(Object obj) {
                        ViewTree viewTree = HookHelper.getViewTree(decorView);
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
    };
}
