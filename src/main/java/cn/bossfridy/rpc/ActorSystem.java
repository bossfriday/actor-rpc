package cn.bossfridy.rpc;

import cn.bossfridy.rpc.dispatch.ActorDispatcher;
import cn.bossfridy.rpc.interfaces.IActorMsgDecoder;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.mailbox.MessageInBox;
import cn.bossfridy.rpc.mailbox.MessageSendBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActorSystem {
    @Getter
    private MessageInBox inBox;

    @Getter
    private MessageSendBox sendBox;

    @Getter
    private ActorDispatcher dispatcher;

    @Getter
    private IActorMsgEncoder msgEncoder;

    @Getter
    private IActorMsgDecoder msgDecoder;

    @Getter
    private Config conf;

    private String name;
    private boolean isStarted = false;
}
