package cn.bossfridy;

import cn.bossfridy.conf.ServiceConfig;
import cn.bossfridy.plugin.IPlugin;
import cn.bossfridy.plugin.PluginElement;
import cn.bossfridy.register.ActorRegister;
import cn.bossfridy.register.ActorRoute;
import cn.bossfridy.router.ClusterRouterBuilder;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.utils.ClassLoaderUtil;
import cn.bossfridy.utils.XmlParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.List;
import java.util.Set;

@Slf4j
public abstract class Bootstrap implements IPlugin {

    /**
     * start
     */
    protected abstract void start() throws Exception;

    /**
     * stop
     */
    protected abstract void stop() throws Exception;

    @Override
    public void startup(String serviceConfigFilePath) {
        try {
            ServiceConfig config = XmlParserUtil.parse(serviceConfigFilePath, ServiceConfig.class);
            log.info("currentNode service-config:" + config.toString());
            ClusterRouterBuilder.build(config);

            // 有配置走配置，无配置反射获取当前jar包内所有UntypedActor类
            List<Class<? extends UntypedActor>> classList = null;
            List<PluginElement> pluginElements = config.getPluginElements();
            if (pluginElements != null && pluginElements.size() > 0) {
                for (PluginElement pluginConfig : pluginElements) {
                    List<Class<? extends UntypedActor>> list = ClassLoaderUtil.getAllClass(pluginConfig.getPath(), UntypedActor.class);
                    classList.addAll(list);
                }
            } else {
                Set<Class<? extends UntypedActor>> set = new Reflections().getSubTypesOf(UntypedActor.class);
                classList.addAll(set);
            }

            // registerActor
            classList.forEach(cls -> {
                if (cls.isAnnotationPresent(ActorRoute.class)) {
                    ActorRoute route = cls.getAnnotation(ActorRoute.class);
                    if (route.methods() != null && route.methods().length > 0) {
                        boolean isRegisterByPool = !"".equals(route.poolName()) && route.poolSize() > 0;
                        for (String method : route.methods()) {
                            try {
                                if (isRegisterByPool) {
                                    ActorRegister.registerActor(method, cls, route.min(), route.max(), route.poolName(), route.poolSize());
                                } else {
                                    ActorRegister.registerActor(method, cls, route.min(), route.max());
                                }

                                log.info("registerActor done: " + cls.getSimpleName());
                            } catch (Exception ex) {
                                log.error("registerActor error!", ex);
                            }
                        }
                    }
                }
            });

            ClusterRouterBuilder.getClusterRouter().publishMethods();
            start();
            log.info("Bootstrap.startup() done.");
        } catch (Exception e) {
            log.error("service startup error!", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            stop();
        } catch (Exception e) {
            log.error("service shutdown error!", e);
        }
    }
}
