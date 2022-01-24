package cn.bossfridy.cluster;

import cn.bossfridy.zk.ZkChildrenChangeListener;
import cn.bossfridy.zk.ZkHandler;
import cn.bossfridy.rpc.ActorSystem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class SystemCluster {
    private String systemName;
    private String dataCenterName;
    private String nodeName;
    private String basePath;
    private String dataCenterHomePath;
    private String clusterNodeHomePath;

    private ActorSystem actorSystem;
    private ZkHandler zkHandler;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public SystemCluster(String zkAddress, String systemName, String dataCenterName, String nodeName) throws Exception {
        if (StringUtils.isEmpty(zkAddress))
            throw new Exception("zkAddress isEmpty!");

        if (StringUtils.isEmpty(systemName))
            throw new Exception("systemName isEmpty!");

        if (StringUtils.isEmpty(dataCenterName))
            throw new Exception("dataCenterName isEmpty!");

        this.systemName = systemName;
        this.dataCenterName = dataCenterName;
        this.nodeName = nodeName;
        this.zkHandler = new ZkHandler(zkAddress);

        this.basePath = "/" + systemName + "/" + dataCenterName;
        this.clusterNodeHomePath = basePath + "/clusterNodes";
        this.dataCenterHomePath = "/" + systemName + "/dataCenters";

        this.loadDataCenter();
        this.onDataCenterChanged();

        this.loadClusterNode();
        this.onClusterNodeChanged();

        this.initActorSystem();
    }

    private void loadDataCenter() {
        try {
            if (!this.zkHandler.checkExist(dataCenterHomePath)) {
                this.zkHandler.addPersistedNode(dataCenterHomePath, System.currentTimeMillis());
            }

            List<String> dcNodeList = this.zkHandler.getChildNodeList(dataCenterHomePath);
        } catch (Exception e) {
            log.error("SystemCluster.loadDataCenter() error!", e);
        }
    }

    private void onDataCenterChanged() throws Exception {
        try {
            final SystemCluster cluster = this;
            this.zkHandler.addListener4Children(dataCenterHomePath, new ZkChildrenChangeListener() {
                @Override
                public void added(String path, byte[] data) {
                    cluster.loadDataCenter();
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
            log.error("SystemCluster.onDataCenterChanged() error!", e);
        }
    }

    private void loadClusterNode() {

    }

    private void onClusterNodeChanged() {
        try {
            final SystemCluster cluster = this;
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
            log.error("SystemCluster.onClusterNodeChanged() error!", e);
        }

    }

    private void initActorSystem() {

    }
}
