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

    public byte[] getBuffer()
    {
        return mBuffer;
    }

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
        if(outputBufferIndex>=0){
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
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
                    _decode(data);
//                    outputStream.write(data, 0, data.length);
//                    byte[] receivedData = outputStream.toByteArray();
//                    int nextFrameIndex = findByFrame(receivedData, 1, receivedData.length);
//                    if(nextFrameIndex >= 0){
//                        byte[] frameData = Arrays.copyOfRange(receivedData, 0, nextFrameIndex);
//                        _decode(frameData);
//                        outputStream.reset();
//                        try {
//                            outputStream.write(Arrays.copyOfRange(receivedData, nextFrameIndex, receivedData.length));
//                        } catch (IOException ignored) {
//
//                        }
//                    }
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


}
