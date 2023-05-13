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

final class VideoEncoder extends CameraEncoder implements Runnable {
    private final static String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final long DEFAULT_TIMEOUT_US = 10000;
    public static final int ENCODED = 1;
    private final Size mSize;
    private final Callback mCallback;
    private MediaCodec mMediaCodec;
    private boolean mIsRunning = false;
    private Thread mThread;
    private Handler mMainHandler;
    private Handler mEncoderHandler;

    public VideoEncoder(Size size, Callback callback)
    {
        mSize = size;
        mCallback = callback;

        mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ENCODED) {
                    byte[] data = (byte[]) msg.obj;
                    callback.onEncoded(data);
                }
            }
        };
    }

    public void start(){
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mSize.getWidth(), mSize.getHeight());
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mSize.getWidth() * mSize.getHeight());
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();

            mThread = new Thread(this);
            mThread.start();

            mIsRunning = true;
            return;
        } catch (Exception e) {
            Log.e("CameraHook", "VideoEncoder start failed",e);
        }
        mIsRunning = false;
    }

    public void stop() {
        if(mIsRunning)
        {
            mIsRunning = false;
            mEncoderHandler.getLooper().quit();
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
    public void encode(Image image)
    {
        if(mIsRunning){
            byte[] buffer = ImageUtils.YUV_420_888toNV21(image);
            mEncoderHandler.obtainMessage(ENCODED, buffer).sendToTarget();
        }
        else{
            throw new IllegalStateException("VideoEncoder is not running");
        }
    }

    private void encode(byte[] yuv,long presentationTimeUs)
    {
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(DEFAULT_TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(yuv);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            if(mCallback!=null){
                byte[] data = new byte[bufferInfo.size];
                outputBuffer.get(data,bufferInfo.offset,bufferInfo.size);
                mMainHandler.obtainMessage(ENCODED,data).sendToTarget();
            }
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT_US);
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        mEncoderHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ENCODED) {
                    byte[] data = (byte[]) msg.obj;
                    long presentationTimeUs = System.nanoTime()/1000;
                    encode(data, presentationTimeUs);
                }
            }
        };
        Looper.loop();
    }


    public interface Callback {
        void onEncoded(byte[] data);
    }
}

