package cn.bossfridy.rpc;

import cn.bossfridy.rpc.queues.MessageReceiver;
import cn.bossfridy.rpc.queues.MessageSender;
import cn.bossfridy.rpc.interfaces.IActorMsgDecoder;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import lombok.Getter;

public class ActorSystem {
    private String name;

    @Getter
    private MessageReceiver receiver;

    @Getter
    private MessageSender sender;

    @Getter
    private IActorMsgEncoder msgEncoder;

    @Getter
    private IActorMsgDecoder msgDecoder;

    @Getter
    private Config conf;
}
