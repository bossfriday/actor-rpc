package cn.bossfridy.router;

import cn.bossfridy.conf.ServiceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterRouterBuilder {
    private static volatile ClusterRouter clusterRouter;

    /**
     * build
     */
    public static void build(ServiceConfig serviceConfig) throws Exception {
        if (clusterRouter == null) {
            synchronized (ClusterRouterBuilder.class) {
                if (clusterRouter == null) {
                    clusterRouter = new ClusterRouter(serviceConfig.getSystemName(),
                            serviceConfig.getZkAddress(),
                            serviceConfig.getClusterNode().getName(),
                            serviceConfig.getClusterNode().getHost(),
                            serviceConfig.getClusterNode().getPort(),
                            serviceConfig.getClusterNode().getVirtualNodesNum());
                }
            }
        }
    }

    /**
     * getClusterRouter
     */
    public static ClusterRouter getClusterRouter() throws Exception {
        if (clusterRouter == null)
            throw new Exception("plz invoke build() firstly!");

        return clusterRouter;
    }
}
