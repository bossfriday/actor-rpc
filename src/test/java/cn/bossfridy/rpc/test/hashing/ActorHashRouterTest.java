package cn.bossfridy.rpc.test.hashing;

import cn.bossfridy.hashing.ActorHashRouter;
import cn.bossfridy.router.ClusterNode;
import cn.bossfridy.utils.UUIDUtil;

import java.util.*;

/**
 * @ClassName: ActorHashRouterTest
 * @Auther: chenx
 * @Description:
 */
public class ActorHashRouterTest {
    public static void main(String[] args) throws Exception {
        List<ClusterNode> nodeList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String name = "node" + i;
            String host = "127.0.0." + random.nextInt(10);
            int port = random.nextInt(10000);
            ClusterNode node = new ClusterNode(name, 10, host, port);

            for (int j = 0; j < 3; j++) {
                node.addMethod("actor" + j);
            }

            nodeList.add(node);
        }

        ActorHashRouter actorHashRouter = new ActorHashRouter<ClusterNode>(nodeList);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String key = UUIDUtil.getUUID().toString();
            ClusterNode targetNode = (ClusterNode) actorHashRouter.getRouter(key);
        }
        System.out.println(System.currentTimeMillis() - begin);
    }
}
