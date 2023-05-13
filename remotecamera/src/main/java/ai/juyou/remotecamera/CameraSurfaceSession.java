package ai.juyou.remotecamera;

import android.view.Surface;

public class CameraSurfaceSession extends CameraSession {
    private final SurfaceEncoder mSurfaceEncoder;
    private final SurfaceDecoder mSurfaceDecoder;
    public CameraSurfaceSession(SurfaceEncoder surfaceEncoder, SurfaceDecoder surfaceDecoder) {
        this.mSurfaceEncoder = surfaceEncoder;
        this.mSurfaceDecoder = surfaceDecoder;
    }

    public Surface getEncodeSurface(){
        return this.mSurfaceEncoder.getSurface();
    }
}
