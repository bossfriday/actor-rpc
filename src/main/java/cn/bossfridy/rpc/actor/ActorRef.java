package cn.bossfridy.rpc.actor;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.mailbox.MessageSendBox;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.rpc.utils.ObjectCodecUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class ActorRef {
    private String host;
    private int port;
    private String method;

    @Getter
    private byte[] session;

    private MessageSendBox sendBox;
    private IActorMsgEncoder tellEncoder;
    private ActorSystem actorSystem;
    private UntypedActor callbackActor;
    private long ttl;

    public ActorRef() {

    }

    public ActorRef(String host, int port, byte[] session, ActorSystem actorSystem, UntypedActor callbackActor, long ttl) {
        this.host = host;
        this.port = port;
        this.session = session;
        this.actorSystem = actorSystem;
        this.callbackActor = callbackActor;
        this.ttl = ttl;
        if (this.actorSystem != null) {
            this.sendBox = this.actorSystem.getSendBox();
            this.tellEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    public ActorRef(String host, int port, byte[] session, String method, ActorSystem actorSystem) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.session = session;
        this.actorSystem = actorSystem;
        if (this.actorSystem != null) {
            this.sendBox = this.actorSystem.getSendBox();
            this.tellEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    /**
     * tell
     */
    public void tell(Object message, ActorRef sender) {
        if (sender == null) {
            log.info("Can not specify sender when tell.");

            return;
        }

        if (this.sendBox != null) {
            Message msg = new Message();
            msg.setSession(this.session);
            msg.setTargetHost(this.host);
            msg.setTargetPort(this.port);
            msg.setTargetMethod(this.method);
            msg.setSourceHost(sender.host);
            msg.setSourcePort(sender.port);
            msg.setSourceMethod(sender.method);
            msg.setData(tellEncode(message));

            this.registerCallBackActor(this.session);
            sender.registerCallBackActor(this.session);
            this.sendBox.put(msg);
        }
    }

    /**
     * registerCallBackActor
     */
    public void registerCallBackActor(byte[] session) {
        if (this.callbackActor != null) {
            this.actorSystem.getDispatcher().registerCallBackActor(session, callbackActor, ttl);
        }
    }

    /**
     * noSender
     */
    public static ActorRef noSender() {
        return DeadLetterActorRef.Instance;
    }

    private byte[] tellEncode(Object message) {
        byte[] bytes = null;
        if (this.tellEncoder != null) {
            bytes = this.tellEncoder.encode(message);
        } else if (message instanceof byte[]) {
            bytes = (byte[]) message;
        } else if (message instanceof Serializable) {
            try {
                bytes = ObjectCodecUtil.encode(message);
            } catch (Exception e) {
                log.error("tellEncode() error!", e);
            }
        } else {
            log.error("Can not encode this obj.");
            throw new RuntimeException("Can not encode this obj.");
        }

        return bytes;
    }
}
