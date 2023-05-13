package ai.juyou.remotecamera;

import android.media.Image;

public abstract class CameraDecoder {
    protected Callback mCallback;
    public abstract void start();
    public abstract void stop();
    public abstract void decode(byte[] data);
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
    public interface Callback {
        void onDecoded(byte[] data);
    }
}
