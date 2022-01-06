package cn.bossfridy.rpc.config;

import lombok.Getter;
import lombok.Setter;

public class Config {
    public static final int EACH_RECEIVE_QUEUE_SIZE = 1024 * 1024;
    public static final int EACH_SEND_QUEUE_SIZE = 1024 * 1024;

    @Setter
    @Getter
    private String ip = "127.0.0.1";

    @Setter
    @Getter
    private int port = 8899;

    @Setter
    @Getter
    private long defaultCallbackActorTtl = 5000;

    @Setter
    @Getter
    private long intervalOfCallbackFilter = 1000;

    @Setter
    @Getter
    private boolean isSingleMode = true;
}
