package ai.juyou.remotecamera;

public class RemoteCamera {

    private CameraPushImpl mCameraPush;
    private ServerConfig mServerConfig;
    private PushCallback mPushCallback;

    public RemoteCamera() {
        mServerConfig = new ServerConfig("192.168.0.222",8020);
        mCameraPush = new CameraPushImpl(mServerConfig, new CameraPushImpl.Callback() {
            @Override
            public void onConnected() {
                if(mPushCallback != null)
                    mPushCallback.onConnected(mCameraPush);
            }

            @Override
            public void onConnectFailed() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onReceived(byte[] data) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void Open()
    {
        mCameraPush.connect();
    }

    public void Close()
    {

    }

    public void setPushCallback(PushCallback callback) {
        mPushCallback = callback;
    }
}
