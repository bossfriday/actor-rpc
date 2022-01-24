package cn.bossfridy.cluster;

import cn.bossfridy.hashing.BaseClusterNode;
import cn.bossfridy.utils.ByteUtil;
import cn.bossfridy.utils.GsonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ClusterNode extends BaseClusterNode<ClusterNode> {
    @Getter
    protected String host;

    @Getter
    protected int port;

    @Getter
    @Setter
    private List<String> methods;

    protected ClusterNode(String name, int virtualNodesNum, String host, int port) {
        super(name, virtualNodesNum);
        this.host = host;
        this.port = port;
    }

    /**
     * addMethod
     */
    public void addMethod(String method) {
        if (this.methods == null)
            this.methods = new ArrayList<String>();
        this.methods.add(method);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClusterNode) {
            ClusterNode node = (ClusterNode) obj;
            return this.host.equals(node.getHost()) && this.port == node.getPort();
        }

        return false;
    }

    @Override
    protected int compareTo(ClusterNode node) {
        int int1 = ByteUtil.ipToInt(this.host);
        int int2 = ByteUtil.ipToInt(node.getHost());
        if (int1 > int2) {
            return 1;
        } else if (int1 == int2) {
            if (this.port > node.getPort()) {
                return 1;
            } else if (this.port == node.getPort()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }

    public static void main(String[] args) {
        List<ClusterNode> nodeList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String name = "node" + i;
            String host = "127.0.0." + random.nextInt(10);
            int port = random.nextInt(10000);
            ClusterNode node = new ClusterNode(name, 1000, host, port);
            nodeList.add(node);
        }

        Collections.sort(nodeList, new Comparator<ClusterNode>() {
            @Override
            public int compare(ClusterNode o1, ClusterNode o2) {
                return o1.compareTo(o2);
            }
        });

        for (ClusterNode node : nodeList) {
            System.out.println(node);
        }
    }
}
