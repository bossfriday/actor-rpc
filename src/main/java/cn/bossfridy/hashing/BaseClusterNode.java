package cn.bossfridy.hashing;

import lombok.Data;

@Data
public abstract class BaseClusterNode<T extends BaseClusterNode> {
    protected String name;  // 节点名称（不重）

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
