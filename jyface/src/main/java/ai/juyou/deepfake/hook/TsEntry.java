package ai.juyou.deepfake.hook;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TsEntry {
    private static final String TAG = "CameraHook";

    HookCamera2 hookCamera;
    private boolean isFirst = true;
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable
    {
        hookCamera = new HookCamera2();
        hookCamera.hook(lpParam);
        XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "onResume: " + param.thisObject);
                Activity activity = (Activity)param.thisObject;
                ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();


            }
        });
    }

}
