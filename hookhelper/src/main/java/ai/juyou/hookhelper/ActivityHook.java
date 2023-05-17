package ai.juyou.hookhelper;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class ActivityHook {
    private static final String TAG = "CameraHook";
    private final List<HookItem> hookResumeItems = new ArrayList<>();

    public void hook(XC_LoadPackage.LoadPackageParam lpParam)
    {
        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "onResume: " + param.thisObject);
                    Activity activity = (Activity)param.thisObject;
                    for(HookItem item : hookResumeItems){
                        if(param.thisObject.getClass().getName().equals(item.activityName)) {
                            if(item.count > 0) {
                                item.callback.onHook(activity);
                                item.count--;
                            }
                        }
                    }
                }
            });
    }

    protected void addResume(String activityName,int count,Callback callback){
        hookResumeItems.add(new HookItem(activityName, count, callback));
    }

    private static class HookItem
    {
        private final String activityName;
        private int count;
        private final Callback callback;
        public HookItem(String activityName,int count,Callback callback)
        {
            this.activityName = activityName;
            this.count = count;
            this.callback = callback;
        }
    }

    protected interface Callback
    {
        void onHook(Activity activity);
    }
}
