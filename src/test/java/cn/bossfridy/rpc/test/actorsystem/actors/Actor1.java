package cn.bossfridy.rpc.test.actorsystem.actors;

import cn.bossfridy.rpc.actor.ActorRef;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.test.actorsystem.BarCluster;
import cn.bossfridy.rpc.test.actorsystem.modules.Foo;
import cn.bossfridy.rpc.test.actorsystem.modules.FooResult;

public class Actor1 extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        try {
            if (msg instanceof FooResult) {
                FooResult result = (FooResult) msg;
                System.out.println("method2 process done, " + result.toString());

                return;
            }

            throw new Exception("unexpected msg type!");
        } finally {
            msg = null;
        }
    }
}
