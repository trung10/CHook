package ai.juyou.deepfake;

import android.util.Log;

import ai.juyou.deepfake.hook.SelfEntry;
import ai.juyou.deepfake.hook.TsEntry;
import ai.juyou.deepfake.hook.WsEntry;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "CameraHook";
    WsEntry wsEntry;
    TsEntry tsEntry;
    SelfEntry selfEntry;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: " + lpParam.packageName);
        if(lpParam.packageName.equals("com.whatsapp")) {
            wsEntry = new WsEntry();
            wsEntry.handleLoadPackage(lpParam);
        }
        if(lpParam.packageName.equals("org.telegram.messenger.web")) {
            tsEntry = new TsEntry();
            tsEntry.handleLoadPackage(lpParam);
        }
        if(lpParam.packageName.equals("ai.juyou.deepfake")) {
            selfEntry = new SelfEntry();
            selfEntry.handleLoadPackage(lpParam);
        }
    }
}
