package ai.juyou.remotecamera;


import android.util.Log;
import android.util.Size;

public class Camera2 extends Camera {
    public Camera2() {
        super();
    }
    @Override
    public void Open(Size size)
    {
        SurfaceEncoder surfaceEncoder = new SurfaceEncoder(size);
        SurfaceDecoder surfaceDecoder = new SurfaceDecoder(size);
        Camera2Session session = new Camera2Session(surfaceEncoder, surfaceDecoder);
        this.PushConnect(size, surfaceEncoder, session);
        this.PullConnect(size, surfaceDecoder, session);
    }
}

