package ai.juyou.remotecamera;

import android.util.Size;

public class RemoteCamera {

    private CameraPush mCameraPush;
    private ServerConfig mServerConfig;
    private PushCallback mPushCallback;
    private PullCallback mPullCallback;

    public RemoteCamera() {
        mServerConfig = new ServerConfig("192.168.0.222",8020);

    }

    public void Open(Size size)
    {
        if(mCameraPush==null)
        {
            mCameraPush = new CameraPush(mServerConfig, size,new CameraPush.Callback() {
                @Override
                public void onConnected(VideoEncoder videoEncoder) {
                    if(mPushCallback != null){
                        mPushCallback.onConnected(videoEncoder);
                    }
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
            mCameraPush.connect();
        }
    }

    public void Close()
    {
        if(mCameraPush != null)
        {
            mCameraPush.disconnect();
            mCameraPush = null;
        }
    }

    public void setPushCallback(PushCallback callback) {
        mPushCallback = callback;
    }

    public void setPullCallback(PullCallback callback) {
        mPullCallback = callback;
    }
}
