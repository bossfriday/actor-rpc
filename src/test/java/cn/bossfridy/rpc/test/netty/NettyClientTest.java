package cn.bossfridy.rpc.test.netty;

import cn.bossfridy.rpc.transport.*;
import cn.bossfridy.utils.UUIDUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class NettyClientTest {
    NettyClientTest() {

    }

    public void test1() {
        NettyClient client = new NettyClient("127.0.0.1", 8090);
        for (int i = 0; i < 10; i++) {
            Message message = new Message();
            message.setVersion((byte) 1);
            message.setSession(UUIDUtil.getUUIDBytes());
            message.setSourceHost("127.0.0.1");
            message.setSourcePort(8723);
            message.setSourceMethod("srcMethod");
            message.setTargetMethod("abc");
            message.setData(new byte[]{(byte) i});

            client.send(message);
        }
    }

    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private Channel channel;
    private String host;
    private int port;
    private long connInitTime;

    public NettyClientTest(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MessageDecoder())
                                .addLast(new MessageEncoder())
                                .addLast(new WriteTimeoutHandler(300))
                                .addLast(new NettyClientHandler());
                    }
                });

        ChannelFuture future = this.bootstrap.connect(this.host, this.port);
        future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                System.out.println("connect failed");
            } else {
                this.channel = f.channel();
                System.out.println("connect ok");
            }
            this.channel = f.channel();
        });
    }

    public static void main(String[] args) {
        NettyClient client = new NettyClient("127.0.0.1", 8090);
        for (int i = 0; i < 10; i++) {
            Message message = new Message();
            message.setVersion((byte) 1);
            message.setSession(UUIDUtil.getUUIDBytes());
            message.setSourceHost("127.0.0.1");
            message.setSourcePort(8723);
            message.setSourceMethod("srcMethod");
            message.setTargetHost("127.0.0.1");
            message.setTargetPort(1234);
            message.setTargetMethod("abc");
            message.setData(new byte[]{(byte) i});

            client.send(message);
        }
    }
}
