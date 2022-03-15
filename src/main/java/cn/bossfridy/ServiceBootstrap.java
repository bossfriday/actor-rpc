package cn.bossfridy;

import cn.bossfridy.conf.ServiceConfig;
import cn.bossfridy.plugin.IPlugin;
import cn.bossfridy.plugin.PluginElement;
import cn.bossfridy.register.ActorRegister;
import cn.bossfridy.register.ActorRoute;
import cn.bossfridy.router.ClusterRouterFactory;
import cn.bossfridy.rpc.actor.UntypedActor;
import cn.bossfridy.utils.ClassLoaderUtil;
import cn.bossfridy.utils.XmlParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.bossfridy.Const.CPU_PROCESSORS;

@Slf4j
public abstract class ServiceBootstrap implements IPlugin {

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
            ServiceConfig config = getServiceConfig(serviceConfigFilePath);
            ClusterRouterFactory.build(config);
            registerActor(config);
            ClusterRouterFactory.getClusterRouter().registryService();
            ClusterRouterFactory.getClusterRouter().startActorSystem();
            start();
            log.info("Bootstrap.startup() done.");
        } catch (Exception ex) {
            log.error("Bootstrap.startup() error!", ex);
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

    private ServiceConfig getServiceConfig(String serviceConfigFilePath) throws Exception {
        ServiceConfig config = XmlParserUtil.parse(serviceConfigFilePath, ServiceConfig.class);
        log.info("currentNode service-config:" + config.toString());

        return config;
    }

    private void registerActor(ServiceConfig config) throws Exception {
        // 有配置走配置，无配置反射获取当前jar包内所有UntypedActor类
        List<Class<? extends UntypedActor>> classList = new ArrayList<>();
        List<PluginElement> pluginElements = config.getPluginElements();
        if (pluginElements != null && pluginElements.size() > 0) {
            for (PluginElement pluginConfig : pluginElements) {
                File file = new File(pluginConfig.getPath());
                if (!file.exists()) {
                    log.warn("service build not existed!(" + pluginConfig.getPath() + ")");
                    continue;
                }

                List<Class<? extends UntypedActor>> list = ClassLoaderUtil.getAllClass(pluginConfig.getPath(), UntypedActor.class);
                classList.addAll(list);
            }
        } else {
            Set<Class<? extends UntypedActor>> set = new Reflections().getSubTypesOf(UntypedActor.class);
            classList.addAll(set);
        }

        // registerActor
        if (classList == null || classList.size() == 0) {
            log.warn("no actor need to register!");
            return;
        }

        classList.forEach(cls -> {
            if (cls.isAnnotationPresent(ActorRoute.class)) {
                ActorRoute route = cls.getAnnotation(ActorRoute.class);
                if (route.methods() != null && route.methods().length > 0) {
                    boolean isRegisterByPool = !"".equals(route.poolName()) && route.poolSize() > 0;
                    for (String method : route.methods()) {
                        try {
                            if (isRegisterByPool) {
                                ActorRegister.registerActor(method, cls, getActorExecutorMin(route), getActorExecutorMax(route), route.poolName(), route.poolSize());
                            } else {
                                ActorRegister.registerActor(method, cls, getActorExecutorMin(route), getActorExecutorMax(route));
                            }

                            log.info("registerActor done: " + cls.getSimpleName());
                        } catch (Exception ex) {
                            log.error("registerActor error!", ex);
                        }
                    }
                }
            }
        });
    }

    private static final int defaultMin;
    private static final int defaultMax;

    static {
        defaultMin = (CPU_PROCESSORS / 2) <= 0 ? 1 : (CPU_PROCESSORS / 2);
        defaultMax = CPU_PROCESSORS;
    }

    private static int getActorExecutorMin(ActorRoute route) {
        if (route.min() > defaultMin)
            return route.min();

        return defaultMin;
    }

    private static int getActorExecutorMax(ActorRoute route) {
        if (route.max() > defaultMax)
            return route.max();

        return defaultMax;
    }
}
