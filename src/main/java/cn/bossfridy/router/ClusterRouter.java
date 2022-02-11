package cn.bossfridy.router;

import cn.bossfridy.rpc.ActorSystem;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.zk.ZkChildrenChangeListener;
import cn.bossfridy.zk.ZkHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ClusterRouter {
    private String basePath;
    private String clusterNodeHomePath;

    private ClusterNode currentWorkerNode;
    private ActorSystem actorSystem;
    private ZkHandler zkHandler;

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
        this.currentWorkerNode = new ClusterNode(nodeName, virtualNodesNum, host, port);
        this.basePath = "/" + systemName;
        this.clusterNodeHomePath = basePath + "/clusterNodes";

        this.loadClusterNode();
        this.onClusterNodeChanged();

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
    public void publishMethods() {

    }

    /**
     * registerActor
     */
    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max) throws Exception {
        this.actorSystem.registerActor(method, min, max, cls);
        this.currentWorkerNode.addMethod(method);
    }

    public void registerActor(String method, Class<? extends UntypedActor> cls, int min, int max, ExecutorService pool) throws Exception {
        this.actorSystem.registerActor(method, min, max, pool, cls);
        this.currentWorkerNode.addMethod(method);
    }

    private void loadClusterNode() {

    }

    private void onClusterNodeChanged() {
        try {
            final ClusterRouter cluster = this;
            this.zkHandler.addListener4Children(clusterNodeHomePath, new ZkChildrenChangeListener() {
                @Override
                public void added(String path, byte[] data) {
                    cluster.loadClusterNode();
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
