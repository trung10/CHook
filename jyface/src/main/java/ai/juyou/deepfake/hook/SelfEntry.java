package ai.juyou.deepfake.hook;

import android.app.Activity;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfEntry {
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable
    {
        XposedHelpers.findAndHookMethod("ai.juyou.deepfake.MainActivity",lpParam.classLoader, "isModuleActivated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }
}
