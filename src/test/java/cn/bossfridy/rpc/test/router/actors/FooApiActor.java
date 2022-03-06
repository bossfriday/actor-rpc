package cn.bossfridy.rpc.test.router.actors;

import cn.bossfridy.register.ActorRoute;
import cn.bossfridy.router.ClusterRouterFactory;
import cn.bossfridy.router.RoutableBean;
import cn.bossfridy.router.RoutableBeanFactory;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.rpc.test.actorsystem.modules.Foo;
import cn.bossfridy.rpc.test.actorsystem.modules.FooResult;

@ActorRoute(methods = "fooApi")
public class FooApiActor extends UntypedActor {

    @Override
    public void onReceive(Object msg) throws Exception {
        // 收到逻辑处理服务的处理结果
        if (msg instanceof FooResult) {
            FooResult result = (FooResult) msg;
            long time = System.currentTimeMillis() - Long.valueOf(result.getMsg());
            System.out.println("process done, time:" + time);
            return;
        }

        // 模拟API接口服务收到请求后调用逻辑处理服务进行处理
        if (msg instanceof Foo) {
//            System.out.println("received a msg:" + msg.toString());
            Foo foo = (Foo) msg;
            foo.setDesc(String.valueOf(System.currentTimeMillis()));
            RoutableBean bean = RoutableBeanFactory.buildRandomRouteBean("processFoo", foo);
            ClusterRouterFactory.getClusterRouter().routeMessage(bean, this.getSelf());
            return;
        }

    }
}
