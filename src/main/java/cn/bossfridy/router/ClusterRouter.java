package cn.bossfridy.router;

import cn.bossfridy.hashing.ActorHashRouter;
import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.utils.GsonUtil;
import cn.bossfridy.zk.ZkChildrenChangeListener;
import cn.bossfridy.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.bossfridy.Const.ZK_PATH_CLUSTER_NODE;

@Slf4j
public class ClusterRouter {
    private String basePath;
    private String clusterNodeHomePath;

    private ClusterNode currentNode;
    private ActorSystem actorSystem;
    private ZkHandler zkHandler;
    private ActorHashRouter actorHashRouter;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public ClusterRouter(String systemName,
                         String zkAddress,
                         String nodeName,
                         String host,
                         int port,
                         int virtualNodesNum) throws Exception {
        this.zkHandler = new ZkHandler(zkAddress);
        this.actorSystem = ActorSystem.create(nodeName, new InetSocketAddress(host, port));
        this.currentNode = new ClusterNode(nodeName, virtualNodesNum, host, port);
        this.basePath = "/" + systemName;
        this.clusterNodeHomePath = basePath + "/" + ZK_PATH_CLUSTER_NODE;

        this.refreshConsistentHashRouter();
        this.onClusterChanged();
        this.initActorSystem();
    }

    /**
     * startActorSystem
     */
    public void startActorSystem() {
        if (!this.actorSystem.isStarted()) {
            this.actorSystem.start();
        }
    }

    /**
     * publishMethods
     */
    public void publishMethods() throws Exception {
        if (currentNode.getMethods() == null || currentNode.getMethods().size() == 0) {
            log.warn(currentNode.getName() + " hasn't methods");
            return;
        }

        String zkNodePath = this.basePath + "/" + ZK_PATH_CLUSTER_NODE + "/" + currentNode.getName();
        if (zkHandler.checkExist(zkNodePath)) {
            zkHandler.deleteNode(zkNodePath);
        }

        String value = GsonUtil.beanToJson(currentNode);
        zkHandler.addEphemeralNode(zkNodePath, value);
        log.info("publishMethods done, path:" + zkNodePath + " , value:" + value);
    }

    /**
     * registerActor
     */
    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max) throws Exception {
        this.actorSystem.registerActor(method, min, max, cls);
        this.currentNode.addMethod(method);
    }

    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max, ExecutorService pool) throws Exception {
        this.actorSystem.registerActor(method, min, max, pool, cls);
        this.currentNode.addMethod(method);
    }

    /**
     * getTargetNode
     */
    public ClusterNode getTargetNode(String method, String targetResourceId) {
        this.readLock.lock();
        try {

        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * getTargetNodeList（集群广播使用）
     */
    public List<ClusterNode> getTargetNodeList(String method) {
        this.readLock.lock();
        try {

        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * 刷新集群一致性哈希路由
     */
    private void refreshConsistentHashRouter() {
        this.writeLock.lock();
        try {
            List<String> clusterNodeNameList = this.zkHandler.getChildNodeList(clusterNodeHomePath);
            if (clusterNodeNameList == null || clusterNodeNameList.size() == 0) {
                return;
            }

            List<ClusterNode> clusterNodes = new ArrayList<>();
            for(String nodeName : clusterNodeNameList) {
                ClusterNode clusterNode = this.zkHandler.getData(clusterNodeHomePath + "/" + nodeName, ClusterNode.class);
                clusterNodes.add(clusterNode);
            }

            if (actorHashRouter == null) {
                actorHashRouter = new ActorHashRouter(clusterNodes);
                return;
            }

            actorHashRouter.refresh(clusterNodes);
        } catch (Exception ex) {
            log.error("loadClusterNode() error!", ex);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * 集群变化
     */
    private void onClusterChanged() {
        try {
            final ClusterRouter cluster = this;
            this.zkHandler.addListener4Children(clusterNodeHomePath, new ZkChildrenChangeListener() {
                @Override
                public void added(String path, byte[] data) {
                    cluster.refreshConsistentHashRouter();
                }

                @Override
                public void updated(String path, byte[] data) {
                    this.added(path, data);
                }

                @Override
                public void removed(String path, byte[] data) {
                    this.added(path, data);
                }

                @Override
                public void connectInitialized() {
                    // just ignore
                }

                @Override
                public void reconnected() {
                    // just ignore
                }

                @Override
                public void suspended() {
                    // just ignore
                }

                @Override
                public void connectLost() {
                    // just ignore
                }
            });
        } catch (Exception e) {
            log.error("ClusterRouter.onClusterNodeChanged() error!", e);
        }

    }

    private void initActorSystem() {

    }
}
