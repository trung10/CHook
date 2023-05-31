package ai.juyou.remotecamera.codec;


import android.media.Image;
import android.view.Surface;

public abstract class CameraEncoder {
    protected Callback mCallback;
    public abstract void start();
    public abstract void stop();
    public abstract void encode(Image image);
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
    public interface Callback
    {
        void onEncoded(byte[] data);
    }
}
