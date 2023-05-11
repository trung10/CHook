package ai.juyou.remotecamera;

import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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

class CameraPushImpl extends CameraPush {

    public static final int CONNECT_SUCCESS = 1;
    public static final int CONNECT_FAILED = 2;
    public static final int MESSAGE_RECEIVED = 3;
    public static final int DISCONNECTED = 4;
    public static final int ERROR = 5;
    private final ServerConfig mConfig;
    private final Handler mHandler;
    private Thread mThread;

    private ChannelFuture channelFuture;

    public CameraPushImpl(ServerConfig config, Callback callback)
    {
        mConfig = config;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECT_SUCCESS:
                        callback.onConnected();
                        break;
                    case CONNECT_FAILED:
                        callback.onConnectFailed();
                        break;
                    case MESSAGE_RECEIVED:
                        callback.onReceived((byte[]) msg.obj);
                        break;
                    case DISCONNECTED:
                        callback.onDisconnected();
                        break;
                    case ERROR:
                        callback.onError((Exception) msg.obj);
                        break;
                }
            }
        };
    }

    public void connect()
    {
        CameraPushHandler cameraPushHandler = new CameraPushHandler();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CameraPushImpl.this.run(cameraPushHandler);
                }
                catch (InterruptedException e) {
                    mHandler.obtainMessage(ERROR,e).sendToTarget();
                }
            }
        });
        mThread.start();
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

    @Override
    public void push(Image image) {

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

    public void send(byte[] data) {
        if (channelFuture != null && channelFuture.channel().isActive()) {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeBytes(data);
            channelFuture.channel().writeAndFlush(buffer);
        }
    }

    public void disconnect() {
        if (channelFuture != null && channelFuture.channel().isActive()) {
            channelFuture.channel().close();
            channelFuture = null;
        }
    }

    public interface Callback {
        void onConnected();
        void onConnectFailed();
        void onDisconnected();
        void onReceived(byte[] data);
        void onError(Exception e);
    }
}
