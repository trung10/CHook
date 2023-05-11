package ai.juyou.remotecamera;

public interface PushCallback {
    void onConnected(CameraPush cameraPush);
    void onDisconnect();
}
