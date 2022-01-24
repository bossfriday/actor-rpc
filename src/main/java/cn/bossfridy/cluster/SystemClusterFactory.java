package cn.bossfridy.cluster;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SystemClusterFactory {
    private static SystemCluster cluster = null;
    private static ReentrantLock lock = new ReentrantLock();

    public static void build(String zkAddress, String systemName, String dataCenterName, String nodeName) throws Exception {
        if (cluster == null) {
            lock.lock();
            try {
                if (cluster == null) {
                    cluster = new SystemCluster(zkAddress, systemName, dataCenterName, nodeName);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public static void build(List<String> zkList, String systemName, String dataCenterName, String nodeName) throws Exception {
        String zkAddress = "";
        if (zkList != null && zkList.size() > 0) {
            for (String str : zkList) {
                zkAddress += str + ",";
            }

            if (zkAddress.endsWith(",")) {
                zkAddress = zkAddress.substring(0, zkAddress.length() - 1);
            }
        }

        build(zkAddress, systemName, dataCenterName, nodeName);
    }

    public static SystemCluster getCluster() {
        if (cluster == null)
            throw new RuntimeException("cluster is null! plz init firstly.");

        return cluster;
    }
}
