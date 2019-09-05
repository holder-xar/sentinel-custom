package com.alibaba.csp.sentinel.config;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * @author holder
 * @date 2019/7/4
 * @Description:
 */
public class SentinelConfigLoader {
    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";

    public static final String SENTINEL_CONFIG = "csp.sentinel.config.file";
    private static String DEFAULT_SENTINEL_CONFIG_FILE = "classpath:sentinel.properties";


    private static Properties properties = new Properties();

    static {
        load();
    }


    private static void load() {

        // JVM 参数优先
        String fileName = System.getProperty(SENTINEL_CONFIG);
        if (StringUtil.isBlank(fileName)) {
            fileName = DEFAULT_SENTINEL_CONFIG_FILE;
        }

        // 添加此条
        Properties p = ConfigUtil.loadProperties(fileName);

        //⬇⬇⬇ old version config file
        if (p == null) {
            String path = addSeparator(System.getProperty(USER_HOME)) + DIR_NAME + File.separator;
            fileName = path + AppNameUtil.getAppName() + ".properties";
            File file = new File(fileName);
            if (file.exists()) {
                p = ConfigUtil.loadProperties(fileName);
            }
        }

        if (p != null && !p.isEmpty()) {
            properties.putAll(p);
        }
        // JVM parameter override file config.
        for (Map.Entry<Object, Object> entry : new CopyOnWriteArraySet<>(System.getProperties().entrySet())) {
            String configKey = entry.getKey().toString();
            String newConfigValue = entry.getValue().toString();
            String oldConfigValue = properties.getProperty(configKey);
            properties.put(configKey, newConfigValue);
            if (oldConfigValue != null) {
                RecordLog.info("[SentinelConfig] JVM parameter overrides {0}: {1} -> {2}", configKey, oldConfigValue, newConfigValue);
            }
        }
    }


    public static Properties getProperties() {
        return properties;
    }

}
