package cn.bossfridy.rpc.test.router.actors;

import cn.bossfridy.register.ActorRoute;
import cn.bossfridy.rpc.actor.ActorRef;
import cn.bossfridy.rpc.actor.TypedActor;
import cn.bossfridy.rpc.test.actorsystem.modules.Foo;
import cn.bossfridy.rpc.test.actorsystem.modules.FooResult;

@ActorRoute(methods = "processFoo")
public class FooProcessActor extends TypedActor<Foo> {
    @Override
    public void onMessageReceived(Foo msg) throws Exception {
        FooResult result = process(msg);
        this.getSender().tell(result, ActorRef.noSender());
    }

    private FooResult process(Foo foo) {
        // just do something
        return FooResult.builder().code(200).msg(foo.getDesc()).request(foo).build();
    }
}
