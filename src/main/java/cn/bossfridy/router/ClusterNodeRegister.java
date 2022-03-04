package cn.bossfridy.router;

import cn.bossfridy.utils.GsonUtil;
import cn.bossfridy.zk.ZkHandler;

import static cn.bossfridy.Const.ZK_PATH_CLUSTER_NODE;

public class ClusterNodeRegister {
    private ZkHandler zkHandler = null;
    private String basePath = null;

    public ClusterNodeRegister(ZkHandler zkHandler, String basePath) {
        this.zkHandler = zkHandler;
        this.basePath = basePath;
    }

    /**
     * addNode
     */
    public synchronized void addClusterNode(ClusterNode node) throws Exception {
        String zkNodePath = this.basePath + "/" + ZK_PATH_CLUSTER_NODE + "/" + node.getName();
        if (zkHandler.checkExist(zkNodePath)) {
            zkHandler.deleteNode(zkNodePath);
        }

        zkHandler.addEphemeralNode(zkNodePath, GsonUtil.beanToJson(node));
    }
}
