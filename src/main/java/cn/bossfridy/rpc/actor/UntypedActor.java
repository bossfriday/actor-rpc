package cn.bossfridy.rpc.actor;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.utils.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class UntypedActor {
    @Setter
    @Getter
    private ActorRef sender;

    @Setter
    @Getter
    private ActorRef self;

    /**
     * onReceive
     */
    public abstract void onReceive(Object msg) throws Exception;

    /**
     * onReceive
     */
    public void onReceive(Message message, ActorSystem actorSystem) throws Exception {
        if (message == null || actorSystem == null) {
            log.warn("UntypedActor.onReceive(msg, actorSystem) returned by msg or actorSystem is null!");

            return;
        }

        this.sender = ActorRef.noSender();
        this.self = ActorRef.noSender();

        if (message.hasSource()) {
            if (message.getSourceMethod() == null) {
                // source is callback actor
                sender = new ActorRef(message.getSourceHost(), message.getSourcePort(), message.getSession(), actorSystem, null, 0);
            } else {
                sender = new ActorRef(message.getSourceHost(), message.getSourcePort(), message.getSession(), message.getSourceMethod(), actorSystem);
                self = new ActorRef(actorSystem.getSelfAddress().getHostName(), actorSystem.getSelfAddress().getPort(), UUIDUtil.getUUIDBytes(), message.getTargetMethod(), actorSystem);
            }
        }

        this.setSender(sender);
        this.setSelf(self);

        try {
            Object msgObj = actorSystem.getMsgDecoder().decode(message.getData());
            this.onReceive(msgObj);
        } catch (Throwable e) {
            this.onFailed(e);
        }
    }

    /**
     * onFailed
     */
    public void onFailed(Throwable cause) {
        if (cause != null) {
            log.error("UntypedActor.onFailed()", cause);
        }
    }

    /**
     * onTimeout
     */
    public void onTimeout(String actorKey) {
        log.warn("actor timeout, actorKey:" + actorKey);
    }

    /**
     * clean
     */
    public void clean() {
        this.sender = null;
        this.self = null;
    }
}
