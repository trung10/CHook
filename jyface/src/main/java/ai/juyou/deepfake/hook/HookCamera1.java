package ai.juyou.deepfake.hook;

import android.hardware.Camera;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class HookCamera1 {

    private static final String TAG = "CameraHook";
    public void hook() {
        XposedHelpers.findAndHookMethod(Camera.class, "startPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "startPreview");
                Camera camera = (Camera)param.thisObject;
                Camera.Size size = camera.getParameters().getPreviewSize();
                Log.d(TAG, "startPreview: " + size.width + "x" + size.height);

            }
        });
    }
}
