package ai.juyou.remotecamera;

public interface CameraCallback {
    void onPushConnected(CameraSession session);
    void onPullConnected(CameraSession session);

    void onPushDisconnected();
    void onPullDisconnected();
}
