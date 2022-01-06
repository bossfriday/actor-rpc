package cn.bossfridy.rpc.queues;

import cn.bossfridy.rpc.transport.Message;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageReceiver {
    private static final int SLOW_PROCESS_THRESHOLD = 500;

    private final LinkedBlockingQueue<Message> receiveQueue = null;
}
