package ai.juyou.remotecamera;

import android.media.Image;

import ai.juyou.remotecamera.codec.CameraEncoder;

public class RemoteCameraPushSession {

    private final CameraEncoder mCameraEncoder;

    public RemoteCameraPushSession(CameraEncoder cameraEncoder) {
        this.mCameraEncoder = cameraEncoder;
    }

    public void push(Image image)
    {
        this.mCameraEncoder.encode(image);
    }
}
