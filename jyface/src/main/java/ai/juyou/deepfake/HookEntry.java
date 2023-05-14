package ai.juyou.deepfake;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final String TAG = "CameraHook";
    WsEntry wsEntry;
    TsEntry tsEntry;
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
    }
}
