package ai.juyou.codec;

public class NativeLib {

    // Used to load the 'codec' library on application startup.
    static {
        System.loadLibrary("codec");
    }

    /**
     * A native method that is implemented by the 'codec' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}