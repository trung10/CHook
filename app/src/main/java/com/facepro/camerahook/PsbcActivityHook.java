package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import ai.juyou.hookhelper.ActivityHook;
import ai.juyou.hookhelper.HookHelper;
import ai.juyou.hookhelper.HttpServer;
import ai.juyou.hookhelper.ViewTree;
import ai.juyou.hookhelper.WaitCallback;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PsbcActivityHook extends ActivityHook {
    private static final String TAG = "CameraHook";

    @Override
    public void hook(XC_LoadPackage.LoadPackageParam lpParam){
        addResume("com.yitong.mbank.psbc.module.home.view.activity.MainActivity", 1, hookMainActivityCallback);
        addResume("com.yitong.mbank.psbc.module.login.view.activity.LoginPswActivity", 1, hookLoginPswActivityCallback);
        addResume("com.yitong.mbank.psbc.module.app.view.activity.FaceCheckAuthActivity", 1, hookFaceCheckAuthActivityCallback);
        addResume("com.tencent.could.huiyansdk.activitys.MainAuthActivity", 1, hookMainAuthActivityCallback);
        super.hook(lpParam);
    }

    private final Callback hookMainActivityCallback = new Callback() {
        @Override
        public void onHook(Activity activity) {
            HttpServer.start(activity);

            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            ViewTree viewTree = HookHelper.getViewTree(decorView);
            ViewGroup view = (ViewGroup)viewTree.getView(45);
            HookHelper.waitGetChildView(view, 0, new WaitCallback() {
                @Override
                public void callback(Object obj) {
                    //Log.d(TAG, "callback: " + obj);
                    ViewGroup view = (ViewGroup)obj;
                    ViewTree viewTree = HookHelper.getViewTree(view);
                    //Log.d(TAG, "viewTree: " + viewTree);
                    view.performClick();
                }
            });
        }
    };

    private final Callback hookLoginPswActivityCallback = new Callback() {
        @Override
        public void onHook(Activity activity) {
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            ViewTree viewTree = HookHelper.getViewTree(decorView);
            //Log.d(TAG, "viewTree: " + viewTree);
            EditText editText = (EditText)viewTree.getView(35);
            editText.setText("18694042031");
            CheckBox checkBox = (CheckBox)viewTree.getView(39);
            checkBox.setChecked(true);
            Button button = (Button)viewTree.getView(44);
            button.performClick();
            //Log.d(TAG, "textView: " + textView.getText());

            HookHelper.waitFindChildView(decorView, "的登录密码",true, new WaitCallback() {
                @Override
                public void callback(Object obj) {
                    //Log.d(TAG, "找到了: " + obj);
                    ViewTree viewTree = HookHelper.getViewTree(decorView);
                    //Log.d(TAG, "viewTree: " + viewTree);
                    EditText editText = (EditText)viewTree.getView(49);
                    editText.setText("w3312422");
                    //HookHelper.getSuperClass(editText.getClass());
                    Button button = (Button)viewTree.getView(58);
                    button.performClick();
                }
            });
        }
    };

    private final Callback hookFaceCheckAuthActivityCallback = new Callback() {
        @Override
        public void onHook(Activity activity) {
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            ViewTree viewTree = HookHelper.getViewTree(decorView);
            Log.d(TAG, "viewTree: " + viewTree);
            CheckBox checkBox = (CheckBox)viewTree.getView(47);
            checkBox.setChecked(true);
        }
    };

    private final Callback hookMainAuthActivityCallback = new Callback() {
        @Override
        public void onHook(Activity activity) {
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            ViewTree viewTree = HookHelper.getViewTree(decorView);
            Log.d(TAG, "viewTree: " + viewTree);
        }
    };
}
