package ai.juyou.remotecamera;


import android.util.Size;
import android.view.Surface;

public class CameraSurface extends Camera {
    private Surface mPreviewSurface;
    public CameraSurface() {
        super();
    }

    public void setPreviewSurface(Surface surface){
        mPreviewSurface = surface;
    }

    @Override
    public void Open(Size size)
    {
        SurfaceEncoder surfaceEncoder = new SurfaceEncoder(size);
        SurfaceDecoder surfaceDecoder = new SurfaceDecoder(size);
        CameraSurfaceSession session = new CameraSurfaceSession(surfaceEncoder, surfaceDecoder);
        this.PushConnect(size, surfaceEncoder, session);
        this.PullConnect(size, surfaceDecoder, session);
    }
}

