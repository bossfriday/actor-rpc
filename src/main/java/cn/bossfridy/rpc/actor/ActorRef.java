package cn.bossfridy.rpc.actor;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.queues.MessageSender;
import cn.bossfridy.rpc.utils.UUIDUtil;

public class ActorRef {
    private String host;
    private int port;
    private String method;
    private byte[] session;
    private MessageSender messageSender;
    private IActorMsgEncoder resMsgEncoder;
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
            this.messageSender = this.actorSystem.getSender();
            this.resMsgEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    public ActorRef(String host, int port, byte[] session, String method, ActorSystem actorSystem) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.session = session;
        this.actorSystem = actorSystem;
        if (this.actorSystem != null) {
            this.messageSender = this.actorSystem.getSender();
            this.resMsgEncoder = this.actorSystem.getMsgEncoder();
        }
    }

    /**
     * tell
     */
    public void tell(Object message, ActorRef sender) {

    }

    /**
     * noSender
     */
    public static ActorRef noSender() {
        return DeadLetterActorRef.Instance;

    }

    private static class DeadLetterActorRef extends ActorRef {
        static final ActorRef Instance = new DeadLetterActorRef();

        public DeadLetterActorRef() {
            super("0.0.0.0", 0, UUIDUtil.getUUIDBytes(), (String) null, null);
        }

        @Override
        public void tell(Object message, ActorRef sender) {
            // it is empty
        }
    }
}
