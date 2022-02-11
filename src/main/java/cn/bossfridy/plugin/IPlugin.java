package cn.bossfridy.plugin;

public interface IPlugin {
    /**
     * startup
     *
     * @param serviceConfigFilePath
     */
    void startup(String serviceConfigFilePath);

    /**
     * shutdown
     */
    void shutdown();
}
