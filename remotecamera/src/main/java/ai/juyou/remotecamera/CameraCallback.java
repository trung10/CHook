package ai.juyou.remotecamera;

public interface CameraCallback {
    void onPushConnected(CameraPushSession session);
    void onPullConnected(CameraPullSession session);
    void onPushDisconnected();
    void onPullDisconnected();
}
