package cn.bossfridy.conf;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ConfigHelper {
    private static volatile ConfigHelper instance;
    private Map<String, Object> configMap = new HashMap<>(32);

    private ConfigHelper() {
        loadConfig();
    }

    /**
     * getInstance
     */
    public static ConfigHelper getInstance() {
        if (instance == null) {
            synchronized (ConfigHelper.class) {
                if (instance == null) {
                    instance = new ConfigHelper();
                }
            }
        }

        return instance;
    }

    /**
     * getConfigValue
     */
    public <T> T getConfigValue(String key) {
        if (!configMap.containsKey(key))
            throw new RuntimeException("config not existed!(key:" + key + ")");

        return (T) configMap.get(key);
    }

    /**
     *
     */
    private void loadConfig() {
        InputStream in = null;
        try {
            in = ConfigHelper.class.getClassLoader().getResourceAsStream("rpcConfig.yaml");
            Map<String, Object> yamlConfigMap = new Yaml().load(in);
            for (String key : yamlConfigMap.keySet()) {
                fillConfigMap(key, yamlConfigMap);
            }
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void fillConfigMap(String key, Map<String, Object> map) {
        String mapKey = key;
        if (key.contains(".")) {
            int index = key.lastIndexOf('.');
            mapKey = key.substring(index + 1, key.length());
        }

        Object value = map.get(mapKey);
        if (value instanceof HashMap) {
            HashMap<String, Object> subMap = (HashMap) value;
            for (String subKey : subMap.keySet()) {
                fillConfigMap(key + "." + subKey, subMap);
            }
        } else {
            configMap.putIfAbsent(key, value);
            log.info("load config: " + key + "=" + value);
        }
    }

    public static void main(String[] args) {
        ConfigHelper.getInstance();
    }
}
