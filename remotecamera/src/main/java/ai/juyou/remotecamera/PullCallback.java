package ai.juyou.remotecamera;

public interface PullCallback {
    void onConnected(CameraDecoder cameraDecoder);
    void onDisconnect();
}
