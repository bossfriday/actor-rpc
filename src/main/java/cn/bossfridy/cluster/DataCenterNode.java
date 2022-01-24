package cn.bossfridy.cluster;

import cn.bossfridy.hashing.BaseClusterNode;

public class DataCenterNode extends BaseClusterNode<DataCenterNode> {

    protected DataCenterNode(String name, int virtualNodesNum) {
        super(name, virtualNodesNum);
    }

    @Override
    protected int compareTo(DataCenterNode node) {
        return 0;
    }
}
