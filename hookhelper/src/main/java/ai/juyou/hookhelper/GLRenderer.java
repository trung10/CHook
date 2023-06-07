package ai.juyou.hookhelper;

import android.view.Surface;

public class GLRenderer {
    public native void glInit(Surface surface);
    public native void glDraw(byte[] yuvData);
    public native void glRelease();
}
