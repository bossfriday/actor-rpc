package cn.bossfridy.rpc.test.actorsystem.actors;

import cn.bossfridy.rpc.actor.ActorRef;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.test.actorsystem.modules.Foo;
import cn.bossfridy.rpc.test.actorsystem.modules.FooResult;

public class Actor2 extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        try {
            if (msg instanceof Foo) {
                this.getSender().tell(process((Foo) msg), ActorRef.noSender());
                return;
            }

            throw new Exception("unexpected msg type!");
        } finally {
            msg = null;
        }
    }

    private FooResult process(Foo foo) {
        // just do something
        return FooResult.builder().code(200).msg("ok").request(foo).build();
    }
}
