package cn.bossfridy.rpc.dispatch;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.interfaces.IExecutor;
import cn.bossfridy.rpc.transport.Message;

import java.util.concurrent.ExecutorService;

public class CallBackActorExecutor implements IExecutor {
    private ExecutorService callBackThreadPool;
    private UntypedActor actor = null;
    private long timeoutTimestamp = 0;

    public CallBackActorExecutor(UntypedActor actor, long ttl, ExecutorService callBackThreadPool) {
        this.actor = actor;
        this.timeoutTimestamp = System.currentTimeMillis() + ttl;
        this.callBackThreadPool = callBackThreadPool;
    }

    /**
     * get TTL ms
     */
    public long ttl() {
        return timeoutTimestamp - System.currentTimeMillis();
    }

    /**
     * onTimeout
     */
    public void onTimeout() {
        this.actor.onTimeout();
    }

    @Override
    public void process(Message message, ActorSystem actorSystem) {
        if (this.actor != null) {
            callBackThreadPool.submit(() -> {
                try {
                    actor.onReceive(message, actorSystem);
                } catch (Exception e) {
                    actor.onFailed(e);
                }
            });
        }
    }

    @Override
    public void destroy() {

    }
}
