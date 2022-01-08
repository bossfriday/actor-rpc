package cn.bossfridy.rpc;

import cn.bossfridy.rpc.dispatch.ActorDispatcher;
import cn.bossfridy.rpc.interfaces.IActorMsgDecoder;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.mailbox.MessageInBox;
import cn.bossfridy.rpc.mailbox.MessageSendBox;
import lombok.Getter;

public class ActorSystem {

    private String name;

    @Getter
    private MessageInBox inBox;

    @Getter
    private MessageSendBox sendBox;

    @Getter
    private ActorDispatcher actorDispatcher;

    @Getter
    private IActorMsgEncoder msgEncoder;

    @Getter
    private IActorMsgDecoder msgDecoder;

    @Getter
    private Config conf;
}
