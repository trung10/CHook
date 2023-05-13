package ai.juyou.remotecamera;

public class Camera2Session extends CameraSession {
    private final SurfaceEncoder mSurfaceEncoder;
    private final SurfaceDecoder mSurfaceDecoder;
    public Camera2Session(SurfaceEncoder surfaceEncoder, SurfaceDecoder surfaceDecoder) {
        this.mSurfaceEncoder = surfaceEncoder;
        this.mSurfaceDecoder = surfaceDecoder;
    }
}
