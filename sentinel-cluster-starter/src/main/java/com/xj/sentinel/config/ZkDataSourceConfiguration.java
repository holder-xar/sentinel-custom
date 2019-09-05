package com.xj.sentinel.config;

import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xj.sentinel.util.ZookeeperConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author holder
 * @date 2019/6/12
 * @Description:
 */

@Configuration
public class ZkDataSourceConfiguration {

    private Logger log = LoggerFactory.getLogger(ZkDataSourceConfiguration.class);

    public static String zookeeperAddress;


    public static String appName;

    private final static String CLASS_PATH = "classpath:";

    private final static String DEFAULT_CONFIG_FIRE_NAME = "sentinel.properties";


    @PostConstruct
    public void init() throws IOException {
        // 初始化sentinel的一些配置项
        setProperties();
        // 流控规则源
        initFlowRuleDataSource();
        // 热点流控规则源
        initParamFlowRuleDataSource();
        // client config
        initClientConfigProperty();

    }

    private void setProperties() throws IOException {
        InputStream in = getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FIRE_NAME);
        Properties prop = new Properties();
        prop.load(in);

        appName = prop.getProperty("sentinel.project.name");
        String dashboardServer = prop.getProperty("sentinel.dashboard.server");
        zookeeperAddress = prop.getProperty("sentinel.zookeeper.address");

        if (appName != null) {
            System.setProperty("project.name", appName);
        }
        if (dashboardServer != null) {
            System.setProperty("csp.sentinel.dashboard.server", dashboardServer);
        }
        log.warn("Init sentinel properties, With appName=[{}] dashboardServer=[{}] zookeeperAddress=[{}]",
                new Object[]{appName, dashboardServer, zookeeperAddress});
    }

    private ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ZkDataSourceConfiguration.class.getClassLoader();
        }
        return classLoader;
    }


    private void initFlowRuleDataSource() {
        ReadableDataSource<String, List<FlowRule>> zookeeperDataSource = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.getFlowRuleZkPath(appName),
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                })
        );
        FlowRuleManager.register2Property(zookeeperDataSource.getProperty());
        log.info("Initialize zk dataSource, flowRuleManager register to property");
    }

    private void initParamFlowRuleDataSource() {
        ZookeeperDataSource<List<ParamFlowRule>> zookeeperDataSource = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.getFlowParamRuleZkPath(appName),
                source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {
                })
        );
        ParamFlowRuleManager.register2Property(zookeeperDataSource.getProperty());
        log.info("Initialize zk dataSource, paramFlowRuleManager register to property");
    }

    private void initClientConfigProperty() {
        ReadableDataSource<String, ClusterClientConfig> clientConfigDs = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.GROUP_ID,
                ZookeeperConfigUtils.getClusterConfigDataId(appName),
                source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {
                }));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDs.getProperty());
        log.info("Initialize zk dataSource, ClusterClientConfig register to property");
    }


}
