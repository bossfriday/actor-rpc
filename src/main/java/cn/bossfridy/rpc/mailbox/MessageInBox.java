package cn.bossfridy.rpc.mailbox;

import cn.bossfridy.rpc.dispatch.ActorDispatcher;
import cn.bossfridy.rpc.interfaces.IMsgHandler;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.rpc.transport.NettyServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfridy.rpc.common.Const.SLOW_QUEUE_THRESHOLD;

@Slf4j
public class MessageInBox extends MailBox {
    private final NettyServer server;
    private ActorDispatcher dispatcher;

    public MessageInBox(int size, int port, ActorDispatcher actorDispatcher) {
        super(new LinkedBlockingQueue<Message>(size));
        this.dispatcher = actorDispatcher;
        this.server = new NettyServer(port, new IMsgHandler() {
            @Override
            public void msgHandle(Message msg) {
                MessageInBox.super.put(msg);
            }
        });
    }

    @Override
    public void start() {
        try {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            this.server.run(bossGroup, workerGroup);

            super.start();
        } catch (Exception e) {
            log.error("MessageInBox start() error!", e);
        }
    }

    @Override
    public void process(Message msg) throws Exception {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - msg.getTimestamp() > SLOW_QUEUE_THRESHOLD) {
            log.warn("slow rpc, " + currentTimestamp + " - " + msg.getTimestamp() + " > " + SLOW_QUEUE_THRESHOLD);
        }

        this.dispatcher.dispatch(msg);
    }

    @Override
    public void stop() {
        try {
            super.isStart = false;
            super.queue.clear();

            if (this.server != null)
                this.server.close();
        } catch (Exception e) {
            log.error("MessageInBox stop() error!", e);
        }
    }
}