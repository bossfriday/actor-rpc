package cn.bossfridy.hashing;

import cn.bossfridy.utils.MurmurHashUtil;

import java.util.*;

/**
 * 一致性哈希集群路由（算法：murmur64，ketama也很常用，暂不考虑设置哈希算法）
 */
public class ClusterRouter<T extends BaseClusterNode> {
    private TreeMap<Long, T> nodes; // 虚拟节点

    /**
     * refresh
     */
    public synchronized void refresh(List<T> clusterNodes) throws Exception {
        if (clusterNodes == null || clusterNodes.size() == 0) {
            throw new RuntimeException("clusterNodes is null or empty!");
        }

        Collections.sort(clusterNodes, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });

        if (nodes == null) {
            nodes = new TreeMap<Long, T>();
        } else {
            nodes.clear();
        }

        for (int i = 0; i != clusterNodes.size(); ++i) {
            final T node = clusterNodes.get(i);
            if (node.getVirtualNodesNum() <= 0) {    // 虚拟节点数<=0表示不对外提供服务
                continue;
            }

            for (int n = 0; n < node.getVirtualNodesNum(); n++) {
                nodes.put(hash("SHARD-" + i + "-NODE-" + n), node);
            }
        }
    }

    /**
     * 获取路由信息
     *
     * @param key
     * @return
     */
    public T getRouter(String key) throws Exception {
        SortedMap<Long, T> tail = nodes.tailMap(hash(key)); // 沿环的顺时针找到一个虚拟节点
        if (tail.size() == 0) {
            return nodes.get(nodes.firstKey());
        }

        return tail.get(tail.firstKey());
    }

    /**
     * 哈希Key
     */
    private Long hash(String key) throws Exception {
        return MurmurHashUtil.hash64(key);
    }
}
