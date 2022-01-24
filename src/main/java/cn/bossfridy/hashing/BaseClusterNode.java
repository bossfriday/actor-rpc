package cn.bossfridy.hashing;

import lombok.Getter;

public abstract class BaseClusterNode<T extends BaseClusterNode> {
    @Getter
    protected String name;  // 节点名称

    @Getter
    protected int virtualNodesNum;  // 虚拟节点数（路由权重控制使用）

    protected BaseClusterNode(String name, int virtualNodesNum) {
        this.name = name;
        this.virtualNodesNum = virtualNodesNum;
    }

    /**
     * compareTo（排序用）
     *
     * @param node
     * @return
     */
    protected abstract int compareTo(T node);
}
