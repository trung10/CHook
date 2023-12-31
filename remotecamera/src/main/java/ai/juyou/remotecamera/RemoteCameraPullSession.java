package ai.juyou.remotecamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;

import ai.juyou.remotecamera.codec.CameraDecoder;
import ai.juyou.remotecamera.codec.VideoDecoder;

public class RemoteCameraPullSession {
    private final CameraDecoder mCameraDecoder;
    private final Size mSize;
    private int[] mColorsBuffer;
    public RemoteCameraPullSession(CameraDecoder cameraDecoder) {
        this.mCameraDecoder = cameraDecoder;
        this.mSize = cameraDecoder.getSize();
        final int width = this.mCameraDecoder.getSize().getWidth();
        final int height = this.mCameraDecoder.getSize().getHeight();
        mColorsBuffer = new int[width * height];
    }

    public void pull(Image image) {
        synchronized (this.mCameraDecoder) {
            byte[] buffer = this.mCameraDecoder.getBuffer();
            final int width = this.mCameraDecoder.getSize().getWidth();
            final int height = this.mCameraDecoder.getSize().getHeight();

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
}
