package cn.bossfridy.zk;

public interface ZkNodeChangeListener {
    /**
     * changed
     *
     * @param bytes
     */
    void changed(byte[] bytes);
}
