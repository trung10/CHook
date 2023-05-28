package ai.juyou.remotecamera;

import android.media.Image;

import java.nio.ByteBuffer;

public class CameraImagePushSession implements CameraPushSession {

    private final VideoEncoder mVideoEncoder;

    public CameraImagePushSession(VideoEncoder videoEncoder) {
        this.mVideoEncoder = videoEncoder;
    }

    @Override
    public void encode(Image image)
    {
        this.mVideoEncoder.encode(image);
    }
}
