package ai.juyou.remotecamera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

class CameraPush {

    public static final int CONNECT_SUCCESS = 1;
    public static final int CONNECT_FAILED = 2;
    public static final int MESSAGE_RECEIVED = 3;
    public static final int DISCONNECTED = 4;
    public static final int ERROR = 5;
    private final ServerConfig mConfig;
    private final Handler mHandler;
    private final CameraEncoder mEncoder;
    private Thread mThread;
    private ChannelFuture channelFuture;

    private long mPushCount = 0;

    public CameraPush(ServerConfig config, CameraEncoder encoder, CameraPushCallback callback)
    {
        mConfig = config;
        mEncoder = encoder;
        mEncoder.setCallback(new CameraEncoder.Callback() {
            @Override
            public void onEncoded(byte[] data) {
                mPushCount++;
                CameraPush.this.send(1,data);
            }
        });
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECT_SUCCESS:
                        mEncoder.start();
                        callback.onConnected();
                        break;
                    case CONNECT_FAILED:
                        mEncoder.stop();
                        callback.onConnectFailed();
                        break;
                    case MESSAGE_RECEIVED:
                        callback.onReceived((byte[]) msg.obj);
                        break;
                    case DISCONNECTED:
                        mEncoder.stop();
                        callback.onDisconnected();
                        break;
                    case ERROR:
                        mEncoder.stop();
                        callback.onError((Exception) msg.obj);
                        break;
                }
            }
        };
    }


    public void send(int cmd, byte[] data) {
        if (channelFuture != null && channelFuture.channel().isActive()) {
            // 计算数据长度并添加到包头
            byte[] header = new byte[8];
            System.arraycopy(intToBytes(data.length), 0, header, 0, 4);
            System.arraycopy(intToBytes(cmd), 0,  header, 4, 4);

            byte[] message = new byte[data.length + header.length];
            System.arraycopy(header, 0, message, 0, header.length);
            System.arraycopy(data, 0, message, header.length, data.length);

            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(message);
            channelFuture.channel().writeAndFlush(buffer);
        }
    }

    private byte[] intToBytes(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte)(i >> 24 & 0xFF);
        b[1] = (byte)(i >> 16 & 0xFF);
        b[2] = (byte)(i >> 8 & 0xFF);
        b[3] = (byte)(i & 0xFF);
        return b;
    }

    public long getPushCount()
    {
        return mPushCount;
    }

    public void connect()
    {
        CameraPushHandler cameraPushHandler = new CameraPushHandler();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CameraPush.this.run(cameraPushHandler);
                }
                catch (InterruptedException e) {
                    mHandler.obtainMessage(ERROR,e).sendToTarget();
                }
            }
        });
        mThread.start();
    }

    public void disconnect() {
        mEncoder.stop();
        if (channelFuture != null && channelFuture.channel().isActive()) {
            channelFuture.channel().close();
            channelFuture = null;
        }
    }

    private void run(ChannelHandler channelHandler) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(mConfig.getServerAddress(), mConfig.getServerPort()))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(@NonNull SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();

                            // 添加LengthFieldBasedFrameDecoder解决粘包问题
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));

                            pipeline.addLast(channelHandler);
                        }
                    });

            channelFuture = bootstrap.connect().addListener(future -> {
                if (future.isSuccess()) {
                    mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
                } else {
                    mHandler.obtainMessage(CONNECT_FAILED,future.cause()).sendToTarget();
                }
            });

            // 添加连接超时机制
            //channelFuture.channel().pipeline().addLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS));

            // 添加连接关闭事件的监听器
            channelFuture.channel().closeFuture().addListener((ChannelFutureListener) future -> {
                mHandler.obtainMessage(DISCONNECTED).sendToTarget();
            });

            channelFuture.channel().closeFuture().sync();
        } catch(Exception e){
            mHandler.obtainMessage(ERROR,e).sendToTarget();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    private class CameraPushHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            mHandler.obtainMessage(MESSAGE_RECEIVED,bytes).sendToTarget();
        }
    }
}
