package ai.juyou.remotecamera;


import android.util.Log;
import android.util.Size;

public class Camera1 extends Camera {

    public Camera1() {
        super();
    }

    @Override
    public void Open(Size size)
    {
        VideoEncoder videoEncoder = new VideoEncoder(size);
        VideoDecoder videoDecoder = new VideoDecoder(size);
        Camera1Session session = new Camera1Session(videoEncoder, videoDecoder);
        this.PushConnect(size, videoEncoder, session);
        this.PullConnect(size, videoDecoder, session);
    }

}
