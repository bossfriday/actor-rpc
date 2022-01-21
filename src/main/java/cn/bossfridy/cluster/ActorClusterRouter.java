package cn.bossfridy.cluster;

import cn.bossfridy.zk.ZkHandler;
import cn.bossfridy.rpc.ActorSystem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ActorClusterRouter {
    private String systemName;
    private String dataCenterName;
    private String nodeName;
    private String basePath;

    private ActorSystem actorSystem;
    private ZkHandler zkHandler;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public ActorClusterRouter(String zkAddress, String systemName, String dataCenterName, String nodeName) throws Exception {
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

        this.initHashRingByZk();
        this.listenMethodsChangeFromZk();
        this.initDatacenterRouter();
        this.listenDatacentersChangeFromZk();
        this.initActorSystem();
    }

    private void initHashRingByZk() {

    }

    private void listenMethodsChangeFromZk() {

    }

    private void initDatacenterRouter() {

    }

    private void listenDatacentersChangeFromZk() {

    }

    private void initActorSystem() {

    }
}
