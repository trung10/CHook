package ai.juyou.remotecamera;

import android.media.Image;
import android.util.Size;

import java.nio.ByteBuffer;

public class Camera1Session extends CameraSession {

    private final VideoEncoder mVideoEncoder;
    private final VideoDecoder mVideoDecoder;
    public Camera1Session(VideoEncoder videoEncoder, VideoDecoder videoDecoder) {
        this.mVideoEncoder = videoEncoder;
        this.mVideoDecoder = videoDecoder;
    }

    public void render(Image image)
    {
        synchronized (this.mVideoDecoder) {
            byte[] buffer = this.mVideoDecoder.getBuffer();
            final int width = this.mVideoDecoder.getSize().getWidth();
            final int height = this.mVideoDecoder.getSize().getHeight();

            ByteBuffer yByteBuffer = image.getPlanes()[0].getBuffer();
            yByteBuffer.position(0);
            yByteBuffer.put(buffer, 0, width*height);

            ByteBuffer uByteBuffer = image.getPlanes()[1].getBuffer();
            uByteBuffer.position(0);
            ByteBuffer vByteBuffer = image.getPlanes()[2].getBuffer();
            vByteBuffer.position(0);

            final int size = width * height;
            final int quarter = size / 4;
            final int vPosition = size; // This is where V starts
            final int uPosition = size + quarter; // This is where U starts

            for (int i = 0; i < quarter; i++) {
                //output[size + i*2 ] = input[vPosition + i]; // For NV21, V first
                //output[size + i*2 + 1] = input[uPosition + i]; // For Nv21, U second
                uByteBuffer.put(i*2, buffer[uPosition + i]);
                if(i*2+1 < uByteBuffer.capacity()){
                    uByteBuffer.put(i*2+1, buffer[vPosition + i]);
                }
                vByteBuffer.put(i*2, buffer[vPosition + i]);
                if(i*2+1 < vByteBuffer.capacity()){
                    vByteBuffer.put(i*2+1, buffer[uPosition + i]);
                }
            }
        }
    }

    public void encode(Image image)
    {
        this.mVideoEncoder.encode(image);
    }
}
