package com.facepro.camerahook;



import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaCodec;
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

    private static final String TAG = "zqf-dev";
    private Surface mSurface;
    private MediaCodec mMediaCodec;
    private Handler mDecodeHandler;
    private boolean mIsRunning = true;

    private byte[] mYuvData = null;

    public VideoDecoder(int width, int height) {
        //初始化解码器
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
                //初始化解码器
                if (initMediaCodec(width, height)) {
                    //启动socket线程
                    startSocketThread();
                }
                Looper.loop();
            }
        };
        decodeThread.start();
    }

    public void setSurface(Surface surface){
        mSurface = surface;
    }

    public void release(){
        mIsRunning = false;
    }

    private boolean initMediaCodec(int width, int height) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mMediaCodec.configure(mediaFormat, null, null, 0);
            mMediaCodec.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
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
            if(mYuvData==null){
                mYuvData = new byte[outBuffer.remaining()];
            }
            outBuffer.get(mYuvData, 0, outBuffer.remaining());
            if(mSurface!=null){
                int width = mMediaCodec.getInputFormat().getInteger(MediaFormat.KEY_WIDTH);
                int height = mMediaCodec.getInputFormat().getInteger(MediaFormat.KEY_HEIGHT);
                byte[] yuvData = RotateNV21(mYuvData,width,height);
                int new_width = height;
                int new_height = width;
                Bitmap image = yuvToBitmap(yuvData,new_width,new_height);
                Canvas canvas = mSurface.lockCanvas(null);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                canvas.drawBitmap(image, null,  new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
                mSurface.unlockCanvasAndPost(canvas);
            }
            mMediaCodec.releaseOutputBuffer(outIndex, false);
        }
    }

    public static byte[] RotateNV21(byte[] nv21, int width, int height) {
        byte[] rotatedNV21 = new byte[nv21.length];
        int frameSize = width * height;
        // Rotate Y component by 270 degrees
        int rotatedIndex = 0;
        for (int x = width - 1; x >= 0; x--)
        {
            for (int y = 0; y < height; y++)
            {
                rotatedNV21[rotatedIndex++] = nv21[y * width + x];
            }
        }
        // Rotate UV components by 270 degrees
        int uvOffset = frameSize;
        rotatedIndex = uvOffset;
        for (int x = width - 2; x >= 0; x -= 2)
        {
            for (int y = 0; y < height / 2; y++)
            {
                rotatedNV21[rotatedIndex++] = nv21[uvOffset + y * width + x];
                rotatedNV21[rotatedIndex++] = nv21[uvOffset + y * width + x + 1];
            }
        }
        return rotatedNV21;
    }


    public static Bitmap yuvToBitmap(byte[] data, int width, int height) {
        int[] colors = new int[width * height];

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

    private void startSocketThread(){
        Thread socketThread = new Thread() {
            @Override
            public void run() {
                super.run();
                receiveData();
            }
        };
        socketThread.start();
    }
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private void receiveData()
    {
        try {
            socket = new Socket("192.168.3.118", 8010);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            try {
                //发送无符号32位整数
                int num = 111222333;
                byte[] bytes = new byte[4];
                bytes[0] = (byte) ((num >>> 24) & 0xFF);
                bytes[1] = (byte) ((num >>> 16) & 0xFF);
                bytes[2] = (byte) ((num >>> 8) & 0xFF);
                bytes[3] = (byte) (num & 0xFF);
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
                        prehandle(outputStream, data1);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void prehandle(ByteArrayOutputStream outputStream, byte[] receivedData) throws IOException {
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
