package cn.bossfridy.rpc.actor;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.mailbox.MessageSendBox;
import cn.bossfridy.rpc.transport.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActorRef {
    private String host;
    private int port;

    @Getter
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
    public void tell(Object message, ActorRef sender) throws Exception {
        if (sender == null) {
            throw new Exception("sender is null!");
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
            msg.setData(this.tellEncoder.encode(message));

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
}
