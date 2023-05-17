package ai.juyou.hookhelper;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class ActivityHook {
    public abstract void hook(XC_LoadPackage.LoadPackageParam lpParam);
}
