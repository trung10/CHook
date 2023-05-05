package com.facepro.camerahook;

import android.app.Activity;
import android.app.Application;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

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
    private boolean isFirst = true;
    private void hookMain(XC_LoadPackage.LoadPackageParam lpParam)
    {
        hookCamera = new HookCamera(lpParam);
        hookCamera.hook1();
        //hookCamera(lpParam);
        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                if(param.thisObject.getClass().getName().equals("com.yitong.mbank.psbc.module.home.view.activity.MainActivity"))
                {
                    if(!isFirst)return;
                    isFirst=false;
                    Activity activity = (Activity)param.thisObject;
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    ViewTree viewTree = HookHelper.getViewTree(decorView);
                    ViewGroup view = (ViewGroup)viewTree.getView(45);
                    HookHelper.waitGetChildView(view, 0, new HookHelper.WaitCallback() {
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
                else if(param.thisObject.getClass().getName().equals("com.yitong.mbank.psbc.module.login.view.activity.LoginPswActivity"))
                {
                    Activity activity = (Activity)param.thisObject;
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

                    HookHelper.waitFindChildView(decorView, "的登录密码",true, new HookHelper.WaitCallback() {
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
                else if(param.thisObject.getClass().getName().equals("com.yitong.mbank.psbc.module.app.view.activity.FaceCheckAuthActivity")){
                    Activity activity = (Activity)param.thisObject;
                    final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    ViewTree viewTree = HookHelper.getViewTree(decorView);
                    //Log.d(TAG, "viewTree: " + viewTree);
                    CheckBox checkBox = (CheckBox)viewTree.getView(47);
                    checkBox.setChecked(true);
                }
            }
        });
    }
}
