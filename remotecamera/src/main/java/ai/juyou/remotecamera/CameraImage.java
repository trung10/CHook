package ai.juyou.remotecamera;


import android.util.Size;

public class CameraImage extends Camera {

    public CameraImage(String ipAddress) {
        super(ipAddress);
    }

    @Override
    public void Open(Size size)
    {
        VideoEncoder videoEncoder = new VideoEncoder(size);
        CameraImagePushSession pushSession = new CameraImagePushSession(videoEncoder);
        this.PushConnect(size, videoEncoder, pushSession);

        VideoDecoder videoDecoder = new VideoDecoder(size);
        CameraImagePullSession pullSession = new CameraImagePullSession(videoDecoder);
        this.PullConnect(size, videoDecoder, pullSession);
    }

}
