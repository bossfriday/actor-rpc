package cn.bossfridy.rpc;

import cn.bossfridy.rpc.actor.ActorRef;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.dispatch.ActorDispatcher;
import cn.bossfridy.rpc.exception.SysException;
import cn.bossfridy.rpc.interfaces.IActorMsgDecoder;
import cn.bossfridy.rpc.interfaces.IActorMsgEncoder;
import cn.bossfridy.rpc.mailbox.MessageInBox;
import cn.bossfridy.rpc.mailbox.MessageSendBox;
import cn.bossfridy.rpc.utils.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;

import static cn.bossfridy.rpc.Const.DEFAULT_CALLBACK_ACTOR_TTL;
import static cn.bossfridy.rpc.Const.EACH_RECEIVE_QUEUE_SIZE;

@Slf4j
public class ActorSystem {
    @Getter
    private String name;

    @Getter
    private MessageInBox inBox;

    @Getter
    private MessageSendBox sendBox;

    @Getter
    private ActorDispatcher dispatcher;

    @Getter
    @Setter
    private IActorMsgEncoder msgEncoder;

    @Getter
    @Setter
    private IActorMsgDecoder msgDecoder;

    @Getter
    private Config conf;

    @Getter
    private boolean isStarted = false;

    private ActorSystem(String name, Config conf) {
        this.name = name;
        this.conf = conf;
        this.dispatcher = new ActorDispatcher(this);
        this.inBox = new MessageInBox(EACH_RECEIVE_QUEUE_SIZE, conf.getPort(), this.dispatcher);
        this.sendBox = new MessageSendBox(inBox, conf);
    }

    /**
     * create
     */
    public static ActorSystem create(String name, Config conf) {
        return new ActorSystem(name, conf);
    }

    /**
     * start
     */
    public void start() {
        this.inBox.start();
        this.sendBox.start();
        this.isStarted = true;
    }

    /**
     * stop
     */
    public void stop() {
        this.dispatcher.stop();
        this.sendBox.stop();
        this.inBox.stop();
        this.isStarted = false;
    }

    /**
     * registerActor
     */
    public void registerActor(String method, int min, int max, ExecutorService pool, Class<? extends UntypedActor> cls, Object... args) throws Exception {
        if (StringUtils.isEmpty(method)) {
            throw new SysException("method is null");
        }

        this.dispatcher.registerActor(method, min, max, pool, cls, args);
    }

    public void registerActor(String method, int min, int max, Class<? extends UntypedActor> cls, Object... args) throws Exception {
        if (StringUtils.isEmpty(method)) {
            throw new SysException("method is null");
        }

        this.dispatcher.registerActor(method, min, max, cls, args);
    }

    /**
     * actorOf(UntypedActor)
     */
    public ActorRef actorOf(long ttl, Class<? extends UntypedActor> cls, Object... args) {
        try {
            if (args == null || args.length == 0) {
                return actorOf(ttl, cls.newInstance());
            }

            Class<?>[] clsArray = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                clsArray[i] = args[i].getClass();
            }
            Constructor<? extends UntypedActor> constructor = cls.getConstructor(clsArray);
            UntypedActor actor = constructor.newInstance(args);

            return actorOf(ttl, actor);
        } catch (Exception e) {
            log.error("ActorSystem.actorOf() error!", e);
        }

        return null;
    }

    public ActorRef actorOf(final long ttl, final UntypedActor actor) {
        return new ActorRef(this.conf.getIp(), this.conf.getPort(), UUIDUtil.getUUIDBytes(), this, actor, ttl);
    }

    public ActorRef actorOf(Class<? extends UntypedActor> cls, Object... args) {
        return actorOf(DEFAULT_CALLBACK_ACTOR_TTL, cls, args);
    }

    public ActorRef actorOf(UntypedActor actor) {
        return actorOf(DEFAULT_CALLBACK_ACTOR_TTL, actor);
    }

    /**
     * actorOf(select ActorRef prepare to tell)
     */
    public ActorRef actorOf(String ip, int port, String targetMethod) {
        byte[] session = UUIDUtil.toBytes(UUIDUtil.getUUID());

        return new ActorRef(ip, port, session, targetMethod, this);
    }

    public ActorRef actorOf(String ip, int port, byte[] session, String targetMethod) {
        return new ActorRef(ip, port, session, targetMethod, this);

    }

    public ActorRef actorOf(byte[] session, String targetMethod) {
        return actorOf(conf.getIp(), conf.getPort(), session, targetMethod);
    }
}
