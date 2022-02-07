package cn.bossfridy.cluster;

import cn.bossfridy.common.Const;
import cn.bossfridy.conf.ConfigHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemClusterFactory {
    private static SystemCluster cluster = null;
    private static boolean initFailedFlag = false;

    static {
        try {
            String systemName = ConfigHelper.getInstance().getConfigValue(Const.SYSTEM_NAME);
            String zkAddress = ConfigHelper.getInstance().getConfigValue(Const.ZK_ADDRESS);
            String nodeName = ConfigHelper.getInstance().getConfigValue(Const.ROUTE_NODE_NAME);
            String host = ConfigHelper.getInstance().getConfigValue(Const.ROUTE_RPC_HOST);
            int port = ConfigHelper.getInstance().getConfigValue(Const.ROUTE_RPC_PORT);
            int virtualNodesNum = ConfigHelper.getInstance().getConfigValue(Const.ROUTE_VIRTUAL_NODES_NUM);
            cluster = new SystemCluster(systemName, zkAddress, nodeName, host, port, virtualNodesNum);
        } catch (Exception e) {
            initFailedFlag = true;
            log.error("SystemClusterFactory init error!", e);
        }
    }

    /**
     * getCluster
     */
    public static SystemCluster getCluster() {
        if (initFailedFlag)
            throw new RuntimeException("SystemClusterFactory init error!");

        return cluster;
    }
}
