package ai.juyou.remotecamera;

public interface PushCallback {
    void onConnected(CameraEncoder cameraPush);
    void onDisconnect();
}
