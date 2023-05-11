package ai.juyou.remotecamera;

public interface PullCallback {
    void onConnect();
    void onDisconnect();
    void onReceiveFrame(byte[] data);
}
