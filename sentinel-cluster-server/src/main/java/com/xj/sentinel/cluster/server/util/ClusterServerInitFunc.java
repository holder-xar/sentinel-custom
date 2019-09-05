package com.xj.sentinel.cluster.server.util;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author: holder
 * @creat: 2019/9/3
 * @Description:
 */
public class ClusterServerInitFunc implements InitFunc {

    private  String appName;

    private static final int CLUSTER_SERVER_PORT = 11111;

    private String zookeeperAddress;

    @Override
    public void init() throws Exception {

        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new ZookeeperDataSource<>(
                    zookeeperAddress,
                    ZookeeperConfigUtils.getFlowRuleZkPath(appName),
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });

        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new ZookeeperDataSource<>(
                    zookeeperAddress,
                    ZookeeperConfigUtils.getFlowParamRuleZkPath(appName),
                    source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
            return ds.getProperty();
        });

        ReadableDataSource<String, Set<String>> namespaceDs = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.namespaceSetDataId,
                source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());


        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.getClusterConfigDataId(appName),
                source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {}));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
    }

    public void start() throws Exception {

        setProp();

        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();

        // A sample for manually load config for cluster server.
        // It's recommended to use dynamic data source to cluster manage config and rules.
        // See the sample in DemoClusterServerInitFunc for detail.
        ClusterServerConfigManager.loadGlobalTransportConfig(new ServerTransportConfig()
                .setIdleSeconds(600)
                .setPort(CLUSTER_SERVER_PORT));
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(appName));

        // Start the server.
        tokenServer.start();
    }



    private Properties setProp() throws IOException {
        Properties prop = new Properties();
        InputStream in = SentinelServer.class.getResourceAsStream("/sentinel.properties");
        prop.load(in);

        appName = prop.getProperty("sentinel.project.name");
        String server = prop.getProperty("sentinel.dashboard.server");
        zookeeperAddress = prop.getProperty("sentinel.zookeeper.address");

        System.setProperty("project.name", appName);
        return prop;
    }
}
