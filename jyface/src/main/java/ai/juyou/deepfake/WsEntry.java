package ai.juyou.deepfake;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import ai.juyou.hookhelper.Utilities;
import ai.juyou.hookhelper.ViewTree;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class WsEntry {

    private static final String TAG = "CameraHook";

    HookCamera2 hookCamera;
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        hookCamera = new HookCamera2();
        hookCamera.hook(lpParam);
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                if(param.thisObject.getClass().getName().equals("com.whatsapp.HomeActivity")) {
                    Activity activity = (Activity)param.thisObject;
                    ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
                    ViewTree viewTree = Utilities.getViewTree(decorView);
                    //Log.d(TAG, "viewTree: " + viewTree);
                    View view = viewTree.getView(21);
                    //Log.d(TAG, "view: " + view);
                    //Utilities.showSuperClass(view.getClass());
                    //viewTree.getView(21).setBackgroundColor(0xff00ff00);
                }
            }
        });
    }
}
