package cn.bossfridy.rpc.test.router;

import cn.bossfridy.ServiceBootstrap;
import cn.bossfridy.plugin.IPlugin;
import cn.bossfridy.router.ClusterRouterFactory;
import cn.bossfridy.router.RoutableBean;
import cn.bossfridy.router.RoutableBeanFactory;
import cn.bossfridy.rpc.actor.ActorRef;
import cn.bossfridy.rpc.test.actorsystem.modules.Foo;

/**
 * @ClassName: Bootstrap
 * @Auther: chenx
 * @Description:
 */
public class Bootstrap extends ServiceBootstrap {
    @Override
    protected void start() throws Exception {

    }

    @Override
    protected void stop() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        ServiceBootstrap plugin = new Bootstrap();
        plugin.startup("service-config.xml");

        for (int i = 0; i < 100; i++) {
            Foo foo = Foo.builder().id(String.valueOf(i)).name("foo" + i).age(i).desc("Foo is a fuck oriented object!").build();
            RoutableBean bean = RoutableBeanFactory.buildRandomRouteBean("fooApi", foo);
            ClusterRouterFactory.getClusterRouter().routeMessage(bean, ActorRef.noSender());
        }

    }
}
