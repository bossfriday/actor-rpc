package cn.bossfridy.cluster.router;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseClusterNode {
    @Getter
    @Setter
    protected String host;

    @Getter
    @Setter
    protected int port;

    /**
     * getVirtualNodesNum（获取虚拟节点数）
     */
    protected abstract int getVirtualNodesNum();

    /**
     * compareTo（排序用）
     *
     * @param node
     * @return
     */
    protected abstract int compareTo(BaseClusterNode node);
}
