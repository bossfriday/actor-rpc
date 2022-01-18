package cn.bossfridy.rpc.transport;

import com.google.common.collect.EvictingQueue;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NettyClient {
    private static final int QUEUE_FIX_SIZE = 10000;
    private static final int CONNECT_TIMEOUT_MILLIS = 10000;

    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private Channel channel;

    private String host;
    private int port;
    private long connInitTime;

    private final EvictingQueue<Message> sendQueue; // 使用有限队列防止异常下OOM
    private AtomicReference<ConnStatus> connState;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        sendQueue = EvictingQueue.create(QUEUE_FIX_SIZE);
        connState = new AtomicReference<>(ConnStatus.CLOSE);
    }

    /**
     * connect
     */
    public void connect() {
        if (connState.get() == ConnStatus.CONNECTING || connState.get() == ConnStatus.CONNECTED) {
            return;
        }

        connState.set(ConnStatus.CONNECTING);
        connInitTime = System.currentTimeMillis();
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
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
                log.warn("NettyClient.connect() failed, target:" + host + ":" + port);

                connState.set(ConnStatus.CLOSE);
                long duration = System.currentTimeMillis() - connInitTime;
                if (duration < 5000) {
                    Thread.currentThread().sleep(3000); // 避免对端断线后不断重连导致cpu负载高
                }

                connect();
                return;
            }

            log.info("NettyClient.connect() success, target:" + host + ":" + port);
            this.channel = f.channel();
            connState.set(ConnStatus.CONNECTED);

            // add close listener
            channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                connState.set(ConnStatus.CLOSE);    // 如果连接被动关闭,一直重连
                connect();
            });

            consumeQueue();
        });
    }

    /**
     * send
     */
    public void send(Message message) {
        switch (connState.get()) {
            case CLOSE:
            case CONNECTING:
                connect();
                insertToQueue(message);
                break;
            case CONNECTED:
                write(message);
                break;
            case CLOSED:
                log.warn("ignore send message, connState:CLOSED");
                return;
        }
    }

    /**
     * close
     */
    public void close() {
        if (this.channel != null) {
            connState.set(ConnStatus.CLOSED);
            this.channel.close();
            this.channel = null;
            this.bootstrap = null;
        }
    }

    private void consumeQueue() {
        if (sendQueue.size() == 0) {
            return;
        }

        Message message;
        while ((message = sendQueue.poll()) != null) {
            write(message);
        }
    }

    private void insertToQueue(Message message) {
        if (sendQueue.size() == QUEUE_FIX_SIZE)
            log.warn("The sendQueue is full and discard an old msg, session:=" + message.getSessionString());

        sendQueue.offer(message);
    }

    private void write(Message message) {
        if (!channel.isActive()) {
            connState.set(ConnStatus.CLOSE);
            insertToQueue(message);
            return;
        }

        try {
            message.buildTimestamp();
            channel.writeAndFlush(message).addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("NettyClient.write() error, target:" + host + ":" + port, future.cause());
                }
            });
        } catch (Exception e) {
            log.error("NettyClient.process() error!", e);
        }
    }

    enum ConnStatus {
        CONNECTING, // 正在连接
        CONNECTED,  // 已连接
        CLOSE,  // 连接关闭（需要重新建立连接）
        CLOSED // 连接被主动关闭
    }
}
