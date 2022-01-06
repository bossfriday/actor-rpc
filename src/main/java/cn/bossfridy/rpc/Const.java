package cn.bossfridy.rpc;

public class Const {
    /**
     * common const
     */
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * thread pool name
     */
    public static final String THREAD_POOL_NAME_ACTORS_DISPATCH = "Actors_Dispatch";
    public static final String THREAD_POOL_NAME_ACTORS_POOLS = "Actors_Pools";
    public static final String THREAD_POOL_NAME_ACTORS_CALLBACK = "Actors_CallBack";
}
