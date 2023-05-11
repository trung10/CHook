package ai.juyou.hookhelper;

public class NativeLib {

    // Used to load the 'hookhelper' library on application startup.
    static {
        System.loadLibrary("hookhelper");
    }

    /**
     * A native method that is implemented by the 'hookhelper' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}