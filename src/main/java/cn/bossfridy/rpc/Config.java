package cn.bossfridy.rpc;

import lombok.Getter;
import lombok.Setter;

public class Config {
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

    /**
     * getSelfAddress
     */
    public String getSelfAddress() {
        return getAddress(this.ip, this.port);
    }

    /**
     * getAddress
     *
     * @param ip
     * @param port
     * @return
     */
    public static String getAddress(String ip, int port) {
        return ip + ":" + port;
    }
}
