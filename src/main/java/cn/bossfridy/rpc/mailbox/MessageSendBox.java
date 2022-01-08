package cn.bossfridy.rpc.mailbox;

import cn.bossfridy.rpc.Config;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.rpc.transport.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfridy.rpc.Const.EACH_SEND_QUEUE_SIZE;

@Slf4j
public class MessageSendBox extends MailBox {
    private MessageInBox inBox;
    private Config conf;
    private ConcurrentHashMap<String, NettyClient> clientMap = new ConcurrentHashMap<String, NettyClient>();    // key:nodeAddress

    public MessageSendBox(MessageInBox inBox, Config conf) {
        super(new LinkedBlockingQueue<Message>(EACH_SEND_QUEUE_SIZE));

        this.inBox = inBox;
        this.conf = conf;
    }

    @Override
    public void process(Message msg) throws Exception {
        if (msg != null) {
            String selfAddress = conf.getSelfAddress();
            String targetAddress = Config.getAddress(msg.getTargetHost(), msg.getTargetPort());

            // 本机通讯：不走网络（直接入接收队列）
            if (selfAddress.equalsIgnoreCase(targetAddress)) {
                inBox.put(msg);

                return;
            }

            // 跨机通讯
            if (!clientMap.containsKey(targetAddress)) {
                NettyClient client = new NettyClient(msg.getTargetHost(), msg.getTargetPort());
                clientMap.putIfAbsent(targetAddress, client);
            }

            clientMap.get(targetAddress).send(msg);
        }
    }

    @Override
    public void stop() {
        try {
            super.isStart = false;
            super.queue.clear();

            for (String key : clientMap.keySet()) {
                NettyClient client = clientMap.get(key);
                client.close();
            }

            clientMap = new ConcurrentHashMap<String, NettyClient>();
        } catch (Exception e) {
            log.error("MessageSendBox.stop() error!", e);
        }
    }
}
