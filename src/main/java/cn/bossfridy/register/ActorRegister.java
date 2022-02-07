package cn.bossfridy.register;

import cn.bossfridy.cluster.SystemClusterFactory;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.utils.ThreadPoolUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ActorRegister {
    private static ConcurrentHashMap<String, ExecutorService> poolMap = new ConcurrentHashMap<>();

    /**
     * registerActor
     *
     * @param method
     * @param cls
     * @param min
     * @param max
     * @param pool
     */
    public static void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max, ExecutorService pool) throws Exception {
        SystemClusterFactory.getCluster().registerActor(method, cls, min, max, pool);
    }

    public static void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max) throws Exception {
        SystemClusterFactory.getCluster().registerActor(method, cls, min, max);
    }

    public static void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max, String poolName, int poolThreadSize) throws Exception {
        if (!poolMap.containsKey(poolName)) {
            ExecutorService pool = ThreadPoolUtil.getThreadPool(poolName, poolThreadSize);
            poolMap.putIfAbsent(poolName, pool);
        }

        registerActor(method, cls, min, max, poolMap.get(poolName));
    }
}
