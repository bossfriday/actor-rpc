package cn.bossfridy.rpc.mailbox;

import cn.bossfridy.rpc.transport.RpcMessage;
import cn.bossfridy.rpc.transport.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.bossfridy.common.Const.EACH_SEND_QUEUE_SIZE;

@Slf4j
public class MessageSendBox extends MailBox {
    private MessageInBox inBox;
    private InetSocketAddress selfAddress;
    private ConcurrentHashMap<InetSocketAddress, NettyClient> clientMap = new ConcurrentHashMap<InetSocketAddress, NettyClient>();

    public MessageSendBox(MessageInBox inBox, InetSocketAddress selfAddress) {
        super(new LinkedBlockingQueue<RpcMessage>(EACH_SEND_QUEUE_SIZE));

        this.inBox = inBox;
        this.selfAddress = selfAddress;
    }

    @Override
    public void process(RpcMessage msg) throws Exception {
        if (msg != null) {
            InetSocketAddress targetAddress = new InetSocketAddress(msg.getTargetHost(), msg.getTargetPort());

            // 本机通讯：不走网络（直接入接收队列）
            if (selfAddress.equals(targetAddress)) {
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

            for (InetSocketAddress key : clientMap.keySet()) {
                NettyClient client = clientMap.get(key);
                client.close();
            }

            clientMap = new ConcurrentHashMap<InetSocketAddress, NettyClient>();
        } catch (Exception e) {
            log.error("MessageSendBox.stop() error!", e);
        }
    }
}
