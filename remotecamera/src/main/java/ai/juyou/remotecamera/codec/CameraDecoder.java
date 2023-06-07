package ai.juyou.remotecamera.codec;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Size;
import android.view.Surface;

public abstract class CameraDecoder {
    protected Callback mCallback;
    public abstract void start();
    public abstract void stop();
    public abstract void decode(byte[] data);
    public abstract byte[] getBuffer();
    public abstract Size getSize();
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
    public interface Callback {
        void onDecoded(byte[] data);
    }
}
