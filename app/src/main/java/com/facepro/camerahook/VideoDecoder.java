package com.facepro.camerahook;




import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class VideoDecoder {
    private static final String TAG = "VideoDecoder";
    private Surface mSurface;
    private MediaCodec mMediaCodec;
    private Handler mDecodeHandler;
    private boolean mIsRunning = true;
    private byte[] mYuvData = null;
    private byte[] mYuvNv21Data = null;
    private byte[] mRotatedNV21 = null;
    private int[] mColorsBuffer;
    private Callback mCallback;
    private Camera mCamera;

    public VideoDecoder() {
    }

    public void setSurface(Surface surface){
        this.mSurface = surface;
    }

    public void setCamera(Camera camera){
        this.mCamera = camera;
    }

    public void setCallback(Callback callback){
        this.mCallback = callback;
    }

    public void start(int width, int height)
    {
        this.mYuvData = new byte[width * height * 3 / 2];
        if(this.mYuvNv21Data==null){
            this.mYuvNv21Data = new byte[width * height * 3 / 2];
        }
        this.mRotatedNV21 = new byte[width * height * 3 / 2];
        mColorsBuffer = new int[width * height];
        //启动socket线程
        Thread decodeThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                mDecodeHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 1) {
                            byte[] frameData = (byte[]) msg.obj;
                            decode(frameData);
                        }
                    }
                };
                try {
                    //初始化解码器
                    if (initMediaCodec(width, height)) {
                        //启动socket线程
                        Thread socketThread = new Thread() {
                            @Override
                            public void run() {
                                receiveData();
                            }
                        };
                        socketThread.start();
                    }
                    Looper.loop();
                }
                finally {
                    releaseMediaCodec();
                }

            }
        };
        decodeThread.start();
        mIsRunning = true;
    }

    public void stop()
    {
        mIsRunning = false;
    }

    public void addCallbackBuffer(byte[] buffer)
    {
        this.mYuvNv21Data = buffer;
    }

    private void releaseMediaCodec(){
        if(mMediaCodec != null){
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    private boolean initMediaCodec(int width, int height) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mMediaCodec.configure(mediaFormat, null, null, 0);
            mMediaCodec.start();
            return true;
        } catch (IOException e) {
            //创建解码失败
            Log.e(TAG, "创建解码失败");
        }
        return false;
    }

    public byte[] getYuvData(){
        return mYuvData;
    }

    private void decode(byte[] frameData)
    {
        int inIndex = mMediaCodec.dequeueInputBuffer(10000);
        if (inIndex >= 0) {
            ByteBuffer inBuffer = mMediaCodec.getInputBuffer(inIndex);
            inBuffer.clear();
            inBuffer.put(frameData, 0, frameData.length);
            mMediaCodec.queueInputBuffer(inIndex, 0, frameData.length, 0, 0);
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outIndex = mMediaCodec.dequeueOutputBuffer(info, 10000);
        if (outIndex >= 0) {
            ByteBuffer outBuffer = mMediaCodec.getOutputBuffer(outIndex);
            outBuffer.get(mYuvData, 0, outBuffer.remaining());

            int width = mMediaCodec.getInputFormat().getInteger(MediaFormat.KEY_WIDTH);
            int height = mMediaCodec.getInputFormat().getInteger(MediaFormat.KEY_HEIGHT);

            i420ToNV21(mYuvData,mYuvNv21Data,width,height);
            if(mSurface!=null){
                //旋转270度
                Bitmap image = yuvToBitmap(mYuvData,mColorsBuffer,width,height);
                image = rotateAndFlipBitmap(image,-90);
                try {
                    Canvas canvas = mSurface.lockCanvas(null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    canvas.drawBitmap(image, null,  new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
                    mSurface.unlockCanvasAndPost(canvas);
                }
                catch (Exception e){
                    Log.e(TAG, "draw error",e);
                }
            }
            if(mCallback!=null){
                mCallback.onFrame(mYuvNv21Data,mCamera);
            }
            mMediaCodec.releaseOutputBuffer(outIndex, false);
        }
    }

    public interface Callback {
        void onFrame(byte[] data, Camera camera);
    }

    /** Convert I420 (YYYYYYYY:UU:VV) to NV21 (YYYYYYYYY:VUVU) */
    public static void i420ToNV21(byte[] i420Data, byte[] nv21Data, int width, int height) {
        int ySize = width * height;
        int uSize = ySize / 4;

        // 将Y分量复制到目标数组中
        System.arraycopy(i420Data, 0, nv21Data, 0, ySize);

        for (int i = 0; i < uSize; i++) {
            int uIndex = ySize + i;
            int vIndex = ySize + uSize + i;

            // 将U和V分量交替写入目标数组
            nv21Data[ySize + i * 2] = i420Data[uIndex];
            nv21Data[ySize + i * 2 + 1] = i420Data[vIndex];
        }

        // 对V分量进行交错格式化
//        for (int i = 0; i < uSize / 2; i++) {
//            int vIndex = ySize + uSize + i * 2;
//            byte temp = nv21Data[vIndex];
//            nv21Data[vIndex] = nv21Data[vIndex + 1];
//            nv21Data[vIndex + 1] = temp;
//        }
    }

    public static byte[] YV12toNV21(final byte[] input,final int width, final int height) {

        final int size = width * height;
        final int quarter = size / 4;
        final int vPosition = size; // This is where V starts
        final int uPosition = size + quarter; // This is where U starts

        byte[] output = new byte[input.length]; // Create a new array with same size
        System.arraycopy(input, 0, output, 0, size); // Y is same

        for (int i = 0; i < quarter; i++) {
            output[size + i*2 ] = input[vPosition + i]; // For NV21, V first
            output[size + i*2 + 1] = input[uPosition + i]; // For Nv21, U second
        }
        return output;
    }

    private static Bitmap rotateAndFlipBitmap(Bitmap source,int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        matrix.preScale(1, -1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private static Bitmap yuvToBitmap(byte[] data,int[] colors, int width, int height) {

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int yIndex = i * width + j;
                int vIndex = (i / 2) * (width / 2) + (j / 2) + width * height;
                int uIndex = (i / 2) * (width / 2) + (j / 2) + width * height + ((width * height) / 4);

                int Y = data[yIndex] & 0xff;
                int U = data[uIndex] & 0xff;
                int V = data[vIndex] & 0xff;

                // 将YUV转换为RGB
                int R = (int) (Y + 1.4075 * (V - 128));
                int G = (int) (Y - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
                int B = (int) (Y + 1.7790 * (U - 128));

                // 计算ARGB值
                R = Math.min(255, Math.max(0, R));
                G = Math.min(255, Math.max(0, G));
                B = Math.min(255, Math.max(0, B));
                colors[yIndex] = 0xff000000 | (R << 16) | (G << 8) | B;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    private byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value>>24) & 0xFF);
        src[2] = (byte) ((value>>16)& 0xFF);
        src[1] = (byte) ((value>>8)&0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private void receiveData()
    {
        try {
            socket = new Socket("192.168.0.222", 8010);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            try {
                //发送无符号32位整数
                int num = 111222333;
                byte[] bytes = new byte[12];
                byte[] lengthBytes = intToBytes(8);
                byte[] cmdBytes = intToBytes(2);
                byte[] numBytes = intToBytes(num);
                System.arraycopy(lengthBytes, 0, bytes, 0, 4);
                System.arraycopy(cmdBytes, 0, bytes, 4, 4);
                System.arraycopy(numBytes, 0, bytes, 8, 4);
                out.write(bytes);
                out.flush();

                //从服务器接收二进制数据
                byte[] data = new byte[8];
                in.read(data);
                data = new byte[1024];

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while (mIsRunning) {
                    int len = in.read(data);
                    if(len>0){
                        byte[] data1 = new byte[len];
                        data1 = Arrays.copyOf(data, len);
                        //decode(data1);
                        preHandle(outputStream, data1);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    //关闭连接
                    in.close();
                    out.close();
                    socket.close();
                    mDecodeHandler.getLooper().quit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void preHandle(ByteArrayOutputStream outputStream, byte[] receivedData) throws IOException {
        if(outputStream.size()+receivedData.length<4){
            //缓冲区的数据加已经接收的数据小于4，继续读取数据
            outputStream.write(receivedData);
            return;
        }
        outputStream.write(receivedData);
        receivedData = outputStream.toByteArray();
        int nextFrameIndex = findByFrame(receivedData, 1, receivedData.length);
        if(nextFrameIndex >= 0){
            byte[] frameData = Arrays.copyOfRange(receivedData, 0, nextFrameIndex);
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = frameData;
            mDecodeHandler.sendMessage(msg);
            outputStream.reset();
            outputStream.write(Arrays.copyOfRange(receivedData, nextFrameIndex, receivedData.length));
        }
    }

    //读取一帧数据
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



