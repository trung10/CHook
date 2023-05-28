package ai.juyou.remotecamera;

import android.util.Log;
import android.util.Size;

public abstract class Camera {
    protected final ServerConfig mPushServerConfig;
    protected final ServerConfig mPullServerConfig;

    private CameraPush mCameraPush;
    private CameraPull mCameraPull;

    private CameraCallback mCallback;

    public Camera(String ipAddress) {
        mPushServerConfig = new ServerConfig(ipAddress,8020);
        mPullServerConfig = new ServerConfig(ipAddress,8030);
    }

    public void setCallback(CameraCallback callback){
        mCallback = callback;
    }

    protected void PushConnect(Size size,CameraEncoder encoder, CameraPushSession session){
        mCameraPush = new CameraPush(mPushServerConfig, encoder, new CameraPushCallback() {
            @Override
            public void onConnected() {
                Log.d("CameraHook", "Push Connect: " + mPushServerConfig.getServerAddress() + ":" + mPushServerConfig.getServerPort());
                if(mCallback!=null){
                    mCallback.onPushConnected(session);
                }
            }
            @Override
            public void onDisconnected() {
                Log.d("CameraHook", "Push disconnect");
                if(mCallback!=null){
                    mCallback.onPushDisconnected();
                }
            }
        });
        mCameraPush.connect();
    }

    protected void PullConnect(Size size,CameraDecoder decoder, CameraPullSession session){
        mCameraPull = new CameraPull(mPullServerConfig, decoder, new CameraPullCallback() {
            @Override
            public void onConnected() {
                Log.d("CameraHook", "Pull Connect: " + mPullServerConfig.getServerAddress() + ":" + mPullServerConfig.getServerPort());
                if(mCallback!=null){
                    mCallback.onPullConnected(session);
                }
            }

            @Override
            public void onDisconnected() {
                Log.d("CameraHook", "Pull disconnect");
                if(mCallback!=null){
                    mCallback.onPullDisconnected();
                }
            }
        });
        mCameraPull.connect();
    }

    public abstract void Open(Size size);

    public void Close()
    {
        if(mCameraPush != null)
        {
            mCameraPush.disconnect();
            mCameraPush = null;
        }
        if(mCameraPull != null)
        {
            mCameraPull.disconnect();
            mCameraPull = null;
        }
    }
}
