package cn.bossfridy.rpc;

public class Const {
    /**
     * thread pool name
     */
    public static final String THREAD_POOL_NAME_ACTORS_DISPATCH = "Actors_Dispatch";
    public static final String THREAD_POOL_NAME_ACTORS_POOLS = "Actors_Pools";
    public static final String THREAD_POOL_NAME_ACTORS_CALLBACK = "Actors_CallBack";

    /**
     * dead letter actor
     */
    public static final String DEAD_LETTER_ACTOR_HOST = "0.0.0.0";
    public static final int DEAD_LETTER_ACTOR_PORT = 0;

    /**
     * queues
     */
    public static final int EACH_RECEIVE_QUEUE_SIZE = 1024 * 1024;
    public static final int EACH_SEND_QUEUE_SIZE = 1024 * 1024;
    public static final int SLOW_QUEUE_THRESHOLD = 500; // ms
}
