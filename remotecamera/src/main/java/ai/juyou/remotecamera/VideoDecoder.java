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
import java.nio.ByteBuffer;

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
                //byte[] data = new byte[bufferInfo.size];
                //outputBuffer.get(data,bufferInfo.offset,bufferInfo.size);
                //mMainHandler.obtainMessage(DECODED,data).sendToTarget();
                Log.d("CameraHook", "onDecoded: " + bufferInfo.size);
            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mDecoderHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == DECODED) {
                    byte[] data = (byte[]) msg.obj;
                    long presentationTimeUs = System.nanoTime()/1000;
                    decode(data, presentationTimeUs);
                }
            }
        };
        Looper.loop();
    }


    public interface Callback {
        void onDecoded(byte[] data);
    }
}
