package ai.juyou.remotecamera;


import android.util.Log;
import android.util.Size;
import android.view.Surface;

import ai.juyou.remotecamera.codec.CameraDecoder;
import ai.juyou.remotecamera.codec.CameraEncoder;
import ai.juyou.remotecamera.codec.VideoDecoder;
import ai.juyou.remotecamera.codec.VideoEncoder;

public class RemoteCamera {
    protected final ServerConfig mPushServerConfig;
    protected final ServerConfig mPullServerConfig;

    private CameraPush mCameraPush;
    private CameraPull mCameraPull;

    private RemoteCameraCallback mCallback;

    public RemoteCamera(String ipAddress) {
        mPushServerConfig = new ServerConfig(ipAddress,8020);
        mPullServerConfig = new ServerConfig(ipAddress,8030);
    }

    public void setCallback(RemoteCameraCallback callback){
        mCallback = callback;
    }

    public void PushConnect(CameraEncoder encoder, RemoteCameraPushSession session){
        mCameraPush = new CameraPush(mPushServerConfig, encoder, new CameraPushCallback() {
            @Override
            public void onConnected() {
                Log.d("CameraHook", "Push Connect: " + mPushServerConfig.getServerAddress() + ":" + mPushServerConfig.getServerPort());
                if(mCallback!=null){
                    mCallback.onPushConnected(session);
                }
            }

            @Override
            public void onConnectFailed(Throwable e) {
                Log.d("CameraHook", "Push Connect Failed");
                if(mCallback!=null){
                    mCallback.onPushConnectFailed(e);
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

    public void PullConnect(CameraDecoder decoder, RemoteCameraPullSession session){
        mCameraPull = new CameraPull(mPullServerConfig, decoder, new CameraPullCallback() {
            @Override
            public void onConnected() {
                Log.d("CameraHook", "Pull Connect: " + mPullServerConfig.getServerAddress() + ":" + mPullServerConfig.getServerPort());
                if(mCallback!=null){
                    mCallback.onPullConnected(session);
                }
            }

            @Override
            public void onConnectFailed(Throwable e) {
                Log.d("CameraHook", "Push Connect Failed");
                if(mCallback!=null){
                    mCallback.onPullConnectFailed(e);
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

    public void open(Size size)
    {
        VideoEncoder videoEncoder = new VideoEncoder(size);
        RemoteCameraPushSession pushSession = new RemoteCameraPushSession(videoEncoder);
        this.PushConnect(videoEncoder, pushSession);

        VideoDecoder videoDecoder = new VideoDecoder(size);
        videoDecoder.setCallback(new CameraDecoder.Callback() {
            @Override
            public void onDecoded(byte[] data) {
                if(mCallback!=null){
                    mCallback.onDecoded(data);
                }
            }
        });
        RemoteCameraPullSession pullSession = new RemoteCameraPullSession(videoDecoder);
        this.PullConnect(videoDecoder, pullSession);
    }

    public void close()
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
