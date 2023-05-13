package ai.juyou.remotecamera;


import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

final class VideoDecoder extends CameraDecoder implements Runnable {
    private final static String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final long DEFAULT_TIMEOUT_US = 10000;
    public static final int DECODED = 1;
    private final Size mSize;
    private final Callback mCallback;
    private MediaCodec mMediaCodec;
    private boolean mIsRunning = false;
    private Thread mThread;
    private Handler mDecoderHandler;
    private byte[] mBuffer;

    public VideoDecoder(Size size, Callback callback)
    {
        mSize = size;
        mCallback = callback;
        mBuffer = new byte[size.getWidth()*size.getHeight()*3/2];
    }

    public void start(){
        try {
            mMediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mSize.getWidth(), mSize.getHeight());
            mMediaCodec.configure(mediaFormat, null, null, 0);
            mMediaCodec.start();

            mThread = new Thread(this);
            mThread.start();

            mIsRunning = true;
            return;
        } catch (Exception e) {
            Log.e("CameraHook", "VideoDecoder start failed",e);
        }
        mIsRunning = false;
    }

    public void stop() {
        if(mIsRunning){
            mIsRunning = false;
            mDecoderHandler.getLooper().quit();
            if(mThread!=null){
                try {
                    mThread.join();
                } catch (InterruptedException ignored) {

                }
                mThread = null;
            }
            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
        }
    }

    @Override
    public void decode(Image image)
    {
        synchronized (this) {
            ByteBuffer yByteBuffer = image.getPlanes()[0].getBuffer();
            yByteBuffer.position(0);
            yByteBuffer.put(mBuffer, 0, mSize.getWidth()*mSize.getHeight());

            ByteBuffer uByteBuffer = image.getPlanes()[1].getBuffer();
            uByteBuffer.position(0);
            ByteBuffer vByteBuffer = image.getPlanes()[2].getBuffer();
            vByteBuffer.position(0);

            final int width = mSize.getWidth();
            final int height = mSize.getHeight();
            final int size = width * height;
            final int quarter = size / 4;
            final int vPosition = size; // This is where V starts
            final int uPosition = size + quarter; // This is where U starts

            for (int i = 0; i < quarter; i++) {
                //output[size + i*2 ] = input[vPosition + i]; // For NV21, V first
                //output[size + i*2 + 1] = input[uPosition + i]; // For Nv21, U second
                uByteBuffer.put(i*2, mBuffer[uPosition + i]);
                if(i*2+1 < uByteBuffer.capacity()){
                    uByteBuffer.put(i*2+1, mBuffer[vPosition + i]);
                }
                vByteBuffer.put(i*2, mBuffer[vPosition + i]);
                if(i*2+1 < vByteBuffer.capacity()){
                    vByteBuffer.put(i*2+1, mBuffer[uPosition + i]);
                }

            }
        }
    }

    public boolean isRunning()
    {
        return mIsRunning;
    }

    public void decode(byte[] data)
    {
        if(mIsRunning){
            mDecoderHandler.obtainMessage(DECODED, data).sendToTarget();
        }
    }

    private void decode(byte[] data,long presentationTimeUs)
    {
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, presentationTimeUs, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            if(mCallback!=null){
                synchronized (this) {
                    outputBuffer.get(mBuffer,bufferInfo.offset,bufferInfo.size);
                }
            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mDecoderHandler = new Handler(Looper.myLooper()) {
            private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == DECODED) {
                    byte[] data = (byte[]) msg.obj;
                    outputStream.write(data, 0, data.length);
                    byte[] receivedData = outputStream.toByteArray();
                    int nextFrameIndex = findByFrame(receivedData, 1, receivedData.length);
                    if(nextFrameIndex >= 0){
                        byte[] frameData = Arrays.copyOfRange(receivedData, 0, nextFrameIndex);
                        long presentationTimeUs = System.nanoTime()/1000;
                        decode(frameData, presentationTimeUs);
                        outputStream.reset();
                        try {
                            outputStream.write(Arrays.copyOfRange(receivedData, nextFrameIndex, receivedData.length));
                        } catch (IOException ignored) {

                        }
                    }
                }
            }
        };
        Looper.loop();
    }

    private int findByFrame(byte[] bytes, int start, int totalSize) {
        for (int i = start; i < totalSize - 4; i++) {
            //对output.h264文件分析 可通过分隔符 0x00000001 读取真正的数据
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }
        }
        return -1;
    }

    public interface Callback {
        void onDecoded(byte[] data);
    }
}
