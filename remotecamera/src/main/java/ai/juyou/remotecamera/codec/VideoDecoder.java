package ai.juyou.remotecamera.codec;


import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class VideoDecoder extends CameraDecoder implements Runnable {
    private final static String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final long DEFAULT_TIMEOUT_US = 10000;
    public static final int DECODED = 1;
    private final Size mSize;
    private MediaCodec mMediaCodec;
    private boolean mIsRunning = false;
    private Thread mThread;
    private Handler mDecoderHandler;
    private byte[] mBuffer;

    public VideoDecoder(Size size)
    {
        mSize = size;
        mBuffer = new byte[size.getWidth()*size.getHeight()*3/2];
    }

    @Override
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

    @Override
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

    public boolean isRunning()
    {
        return mIsRunning;
    }

    @Override
    public byte[] getBuffer()
    {
        return mBuffer;
    }

    @Override
    public Size getSize()
    {
        return mSize;
    }

    @Override
    public void decode(byte[] data)
    {
        if(mIsRunning){
            mDecoderHandler.obtainMessage(DECODED, data).sendToTarget();
        }
    }

    private void _decode(byte[] data)
    {
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        //Log.d("CameraHook", "outputBufferIndex:"+outputBufferIndex);
        if(outputBufferIndex>=0){
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            //Log.d("CameraHook", "outputBuffer:"+outputBuffer.remaining());
            synchronized (this) {
                outputBuffer.get(mBuffer,bufferInfo.offset,bufferInfo.size);
                if(mCallback!=null){
                    mCallback.onDecoded(mBuffer);
                }
            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);

        }
//        while (outputBufferIndex >= 0) {
//            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
//            synchronized (this) {
//                outputBuffer.get(mBuffer,bufferInfo.offset,bufferInfo.size);
//                if(mCallback!=null){
//                    mCallback.onDecoded(mBuffer);
//                }
//            }
//            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
//        }
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
                    Log.d("CameraHook", "handleMessage:"+data.length);
                    _decode(data);
                }
            }
        };
        Looper.loop();
    }
}
