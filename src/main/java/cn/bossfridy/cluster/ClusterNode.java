package cn.bossfridy.cluster;

import cn.bossfridy.cluster.router.BaseClusterNode;
import cn.bossfridy.utils.ByteUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ClusterNode extends BaseClusterNode {
    @Getter
    @Setter
    private String nodeName;

    @Getter
    @Setter
    private List<String> methods;

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
    protected int getVirtualNodesNum() {
        return 1000; // 先hardcode，常规下需要对接配置中心。
    }

    @Override
    protected int compareTo(BaseClusterNode node) {
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
        return "ClusterNode{" +
                "nodeName='" + nodeName + '\'' +
                ", methods=" + methods +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }


    public static void main(String[] args) {
        List<ClusterNode> nodeList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String host = "127.0.0." + random.nextInt(10);
            int port = random.nextInt(10000);
            ClusterNode node = new ClusterNode();
            node.setHost(host);
            node.setPort(port);

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
