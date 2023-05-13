package ai.juyou.remotecamera;

import android.util.Log;
import android.util.Size;

public class RemoteCamera {
    private final ServerConfig mPushServerConfig;
    private final ServerConfig mPullServerConfig;
    private CameraPush mCameraPush;
    private CameraPull mCameraPull;
    private PushCallback mPushCallback;
    private PullCallback mPullCallback;

    public RemoteCamera() {
        mPushServerConfig = new ServerConfig("192.168.0.222",8020);
        mPullServerConfig = new ServerConfig("192.168.0.222",8030);
    }

    public void Open(Size size)
    {
        if(mCameraPush==null)
        {
            mCameraPush = new CameraPush(mPushServerConfig, size,new CameraPushCallback() {
                @Override
                public void onConnected(VideoEncoder videoEncoder) {
                    if(mPushCallback != null){
                        mPushCallback.onConnected(videoEncoder);
                    }
                    Log.d("CameraHook", "Push Connect: " + mPushServerConfig.getServerAddress() + ":" + mPushServerConfig.getServerPort());
                    if(mCameraPull==null)
                    {
                        mCameraPull = new CameraPull(mPullServerConfig, size,new CameraPullCallback() {
                            @Override
                            public void onConnected(VideoDecoder videoDecoder) {
                                if(mPullCallback != null){
                                    mPullCallback.onConnected(videoDecoder);
                                }
                                Log.d("CameraHook", "Pull Connect: " + mPullServerConfig.getServerAddress() + ":" + mPullServerConfig.getServerPort());

                            }

                            @Override
                            public void onDisconnected() {
                                Log.d("CameraHook", "Pull disconnect");

                            }
                        });
                        mCameraPull.connect();
                    }
                }

                @Override
                public void onDisconnected() {
                    Log.d("CameraHook", "Push disconnect");

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
        if(mCameraPull != null)
        {
            mCameraPull.disconnect();
            mCameraPull = null;
        }
    }

    public void setPushCallback(PushCallback callback) {
        mPushCallback = callback;
    }

    public void setPullCallback(PullCallback callback) {
        mPullCallback = callback;
    }
}
