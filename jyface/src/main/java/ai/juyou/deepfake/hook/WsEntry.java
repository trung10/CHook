package ai.juyou.deepfake.hook;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import ai.juyou.hookhelper.HookHelper;
import ai.juyou.hookhelper.ViewTree;
import ai.juyou.hookhelper.WaitCallback;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WsEntry {

    private static final String TAG = "CameraHook";

    HookCamera2 hookCamera;
    private boolean isFirst = true;
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        hookCamera = new HookCamera2();
        hookCamera.hook(lpParam);

        //handleUI(lpParam);
    }

    private void handleUI(XC_LoadPackage.LoadPackageParam lpParam)
    {
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

                if(param.thisObject.getClass().getName().equals("com.whatsapp.HomeActivity")) {
                    HookHelper.waitCall(1000,decorView, new WaitCallback() {
                        @Override
                        public void callback(Object obj) {
                            ViewTree viewTree = HookHelper.getViewTree(decorView);
                            View v = HookHelper.findChildView(decorView,"DD");
                            v = (View)v.getParent().getParent().getParent().getParent();
                            v.performClick();
                        }
                    });
                }
                else if(param.thisObject.getClass().getName().equals("com.whatsapp.Conversation")) {
                    if(!isFirst)return;
                    isFirst=false;
                    HookHelper.waitCall(1000,decorView, new WaitCallback() {
                        @Override
                        public void callback(Object obj) {
//                            ViewTree viewTree = HookHelper.getViewTree(decorView);
//                            View v = viewTree.getView(35);
//                            v.performClick();
                        }
                    });
                }
            }
        });
    }

    private void handleCamera(XC_LoadPackage.LoadPackageParam lpParam)
    {

    }
}
