package ai.juyou.hookhelper;

import android.view.Surface;

public class GLRenderer {
    static {
        System.loadLibrary("hookhelper");
    }
    private Surface mSurface;
    private boolean isInit = false;
    public GLRenderer(Surface surface) {
        this.mSurface = surface;
    }

    public void draw(byte[] yuvData) {
        if(!isInit) {
            glInit(mSurface);
            isInit = true;
        }
        glDraw(yuvData);
    }

    public void release() {
        glRelease();
        if(mSurface!=null) {
            mSurface.release();
            mSurface = null;
        }
    }

    private native void glInit(Surface surface);

    private native void glDraw(byte[] yuvData);

    private native void glRelease();
}
