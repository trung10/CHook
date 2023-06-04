package ai.juyou.hookhelper;

import android.view.Surface;

public class Renderer {
    static {
        System.loadLibrary("hookhelper");
    }

    public native void init(Surface surface);

    public native void resize(int width, int height);

    public native void render();

    public native void release();
}
