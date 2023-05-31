package ai.juyou.remotecamera;

public interface RemoteCameraCallback {
    void onPushConnected(RemoteCameraPushSession session);
    void onPullConnected(RemoteCameraPullSession session);

    void onPushConnectFailed(Throwable e);
    void onPullConnectFailed(Throwable e);

    void onPushDisconnected();
    void onPullDisconnected();
}
