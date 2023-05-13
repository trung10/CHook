package ai.juyou.remotecamera;


import android.util.Size;

public class CameraVideo extends Camera {

    public CameraVideo() {
        super();
    }

    @Override
    public void Open(Size size)
    {
        VideoEncoder videoEncoder = new VideoEncoder(size);
        VideoDecoder videoDecoder = new VideoDecoder(size);
        CameraVideoSession session = new CameraVideoSession(videoEncoder, videoDecoder);
        this.PushConnect(size, videoEncoder, session);
        this.PullConnect(size, videoDecoder, session);
    }

}
