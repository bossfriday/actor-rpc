package cn.bossfridy.rpc.queues;

import cn.bossfridy.rpc.Config;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.rpc.transport.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfridy.rpc.Const.EACH_SEND_QUEUE_SIZE;

@Slf4j
public class MessageSender {
    private final LinkedBlockingQueue<Message> sendQueue;
    private MessageReceiver receiver;
    private Config conf;
    private boolean isStart = true;
    private ConcurrentHashMap<String, NettyClient> clientMap = new ConcurrentHashMap<String, NettyClient>();    // key:nodeAddress

    public MessageSender(MessageReceiver receiver, Config conf) {
        this.sendQueue = new LinkedBlockingQueue<Message>(EACH_SEND_QUEUE_SIZE);
        this.receiver = receiver;
        this.conf = conf;
    }

    /**
     * start
     */
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    try {
                        Message msg = sendQueue.take();
                        if (msg != null) {
                            String selfAddress = conf.getSelfAddress();
                            String targetAddress = Config.getAddress(msg.getTargetHost(), msg.getTargetPort());

                            // 本机通讯：不走网络（直接入接收队列）
                            if (selfAddress.equalsIgnoreCase(targetAddress)) {
                                receiver.addMessage(msg);
                                continue;
                            }

                            // 跨机通讯
                            if (!clientMap.containsKey(targetAddress)) {
                                NettyClient client = new NettyClient(msg.getTargetHost(), msg.getTargetPort());
                                clientMap.putIfAbsent(targetAddress, client);
                            }

                            clientMap.get(targetAddress).send(msg);
                            log.debug(selfAddress + " send a msg to " + targetAddress);
                        }
                    } catch (Exception e) {
                        log.error("MessageSender.start() error!", e);
                    }
                }
            }
        }).start();
    }

    /**
     * send
     */
    public void send(Message msg) {
        try {
            this.sendQueue.put(msg);
        } catch (Exception e) {
            log.error("MessageSender.send() error!", e);
        }
    }

    /**
     * stop
     */
    public void stop() {
        this.isStart = false;
        this.sendQueue.clear();
        for (String key : clientMap.keySet()) {
            NettyClient client = clientMap.get(key);
            client.close();
        }

        clientMap = new ConcurrentHashMap<String, NettyClient>();
    }
}
