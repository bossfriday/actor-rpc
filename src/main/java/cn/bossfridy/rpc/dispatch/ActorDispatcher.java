package cn.bossfridy.rpc.dispatch;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.interfaces.IExecutor;
import cn.bossfridy.rpc.thread.ThreadPoolHelper;
import cn.bossfridy.rpc.transport.Message;
import cn.bossfridy.rpc.utils.UUIDUtil;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.bossfridy.rpc.Const.*;

@Slf4j
public class ActorDispatcher {
    public static final ExecutorService DEFAULT_THREAD_POOL = ThreadPoolHelper.getThreadPool(THREAD_POOL_NAME_ACTORS_POOLS, ThreadPoolHelper.AVAILABLE_PROCESSORS * 2);

    private static final ExecutorService dispatchThreadPool = ThreadPoolHelper.getThreadPool(THREAD_POOL_NAME_ACTORS_DISPATCH, 2);
    private static final ExecutorService callBackThreadPool = ThreadPoolHelper.getThreadPool(THREAD_POOL_NAME_ACTORS_CALLBACK, ThreadPoolHelper.AVAILABLE_PROCESSORS);

    private ConcurrentHashMap<String, IExecutor> actorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IExecutor> callbackActorMap = new ConcurrentHashMap<>();
    private ActorSystem actorSystem;

    public ActorDispatcher(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    /**
     * registerActor
     */
    public void registerActor(String method,
                              int min,
                              int max, Class<? extends UntypedActor> cls,
                              Object... args) {
        ActorExecutor executor = new ActorExecutor(min, max, method, cls, args);
        actorMap.putIfAbsent(method, executor);
    }

    public void registerActor(String method,
                              int min,
                              int max,
                              ExecutorService pool,
                              Class<? extends UntypedActor> cls,
                              Object... args) {
        ActorExecutor executor = new ActorExecutor(min, max, method, pool, cls, args);
        actorMap.putIfAbsent(method, executor);
    }

    /**
     * registerCallBackActor
     */
    public void registerCallBackActor(byte[] key, UntypedActor actor, long ttl) {
        String standKey = UUIDUtil.getShortString(key);
        this.callbackActorMap.putIfAbsent(standKey, new CallBackActorExecutor(actor, ttl, callBackThreadPool));
        HashWheelTimer.putTimeOutTask(new CallBackActorTimerTask(standKey), ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * dispatch
     */
    public void dispatch(Message message) {
        if (message == null)
            return;

        dispatchThreadPool.submit(() -> {
            try {
                IExecutor executor = null;
                if (message.getTargetMethod() == null) {
                    // callback actor
                    byte[] key = message.getSession();
                    executor = this.callbackActorMap.remove(UUIDUtil.getShortString(key));
                } else {
                    // pool actor
                    String method = message.getTargetMethod();
                    executor = this.actorMap.get(method);
                }

                if (executor == null) {
                    throw new Exception("executor is null!");
                }

                executor.process(message, this.actorSystem);
            } catch (Exception e) {
                log.error("ActorDispatcher error!", e);
            }
        });
    }

    /**
     * stop
     */
    public void stop() {
        for (Map.Entry<String, IExecutor> entry : actorMap.entrySet()) {
            entry.getValue().destroy();
        }

        // 不使用hashMap.clear()防止hashMap rehash不缩容导致的OOM
        this.actorMap = new ConcurrentHashMap<>();
        this.callbackActorMap = new ConcurrentHashMap<>();
    }

    /**
     * CallBackActorTimerTask
     */
    private class CallBackActorTimerTask implements TimerTask {
        private String actorKey;

        public CallBackActorTimerTask(String actorKey) {
            this.actorKey = actorKey;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            IExecutor executor = callbackActorMap.get(this.actorKey);
            if (executor instanceof CallBackActorExecutor) {
                CallBackActorExecutor callbackActor = (CallBackActorExecutor) executor;
                long diff = callbackActor.ttl();
                if (diff <= 0) {
                    callbackActorMap.remove(this.actorKey);
                    callbackActor.onTimeout();
                } else {
                    HashWheelTimer.putTimeOutTask(new CallBackActorTimerTask(this.actorKey), diff, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
