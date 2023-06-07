package ai.juyou.hookhelper;

import android.util.Log;
import android.view.Surface;

public class GLRenderer {
    static {
        System.loadLibrary("hookhelper");
    }
    private Surface mSurface;
    private boolean isInit = false;
    private boolean isRelease = false;
    public GLRenderer(Surface surface) {
        this.mSurface = surface;
    }

    public void draw(byte[] yuvData) {
        if(isRelease) {
            return;
        }
        if(!isInit) {
            glInit(mSurface);
            isInit = true;
        }
        if(yuvData!=null){
            glDraw(yuvData);
        }
        else{
            glRelease();
            isRelease = true;
            Log.d("RemoteCamera", "GLRenderer glRelease");
        }
    }

    public void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        isRelease = true;
        Log.d("RemoteCamera", "GLRenderer release");
    }

    private native void glInit(Surface surface);

    private native void glDraw(byte[] yuvData);

    private native void glRelease();
}
