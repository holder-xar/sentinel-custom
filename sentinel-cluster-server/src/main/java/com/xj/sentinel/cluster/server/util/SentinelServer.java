package com.xj.sentinel.cluster.server.util;


import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author holder
 * @date 2019/6/17
 * @Description:
 */
public class SentinelServer{

    private  String appName = AppNameUtil.getAppName();

    private String zookeeperAddress = System.getProperty("sentinel.zookeeper.address");


    /**
     * 初始化工作
     */
    public void init() throws IOException {

//        Properties prop = getProp();
//        appName = prop.getProperty("sentinel.project.name");
//        String server = prop.getProperty("sentinel.dashboard.server");
//        zookeeperAddress = prop.getProperty("sentinel.zookeeper.address");
//
//        System.setProperty("project.name", appName);
//        System.setProperty("csp.sentinel.dashboard.server",server);


        /**
         * 初始化集群限流的Supplier
         * 这样如果后期集群限流的规则发生变更的话，系统可以自动感知到
         */
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<FlowRule>> ds = new ZookeeperDataSource<>(
                    zookeeperAddress,
                    ZookeeperConfigUtils.getFlowRuleZkPath(appName),
                    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
            return ds.getProperty();
        });
        /**
         * 初始化集群热点参数限流的Supplier
         * 这样如果后期集群热点参数限流的规则发生变更的话，系统可以自动感知到
         */
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new ZookeeperDataSource<>(
                    zookeeperAddress,
                    ZookeeperConfigUtils.getFlowParamRuleZkPath(appName),
                    source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
            return ds.getProperty();
        });

        /**
         * 监听 namespace 变化 动态更新
         */
        ReadableDataSource<String, Set<String>> namespaceDs = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.namespaceSetDataId,
                source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());

        /**
         * 加载namespace的集合以及ServerTransportConfig
         * 最好还要再为他们每个都注册一个SentinelProperty，这样的话可以动态的修改这些配置项
         */
        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new ZookeeperDataSource<>(
                zookeeperAddress,
                ZookeeperConfigUtils.getClusterConfigDataId(appName),
                source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {}));
        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());

        // 加载服务端的配置
        loadServerConfig();


    }


    /**
     * 启动ClusterToken服务端
     */
    public void start() throws Exception {
        // 创建一个 ClusterTokenServer 的实例，独立模式
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        tokenServer.start();
    }


    /**
     * 加载namespace的集合以及ServerTransportConfig
     * 最好还要再为他们每个都注册一个SentinelProperty，这样的话可以动态的修改这些配置项
     */
    private void loadServerConfig(){

        // 加载namespace
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(appName));
        // 加载ServerTransportConfig
        ClusterServerConfigManager.loadGlobalTransportConfig(new ServerTransportConfig());
    }



    private Properties getProp() throws IOException {
        Properties prop = new Properties();
        InputStream in = SentinelServer.class.getResourceAsStream("/sentinel.properties");
        prop.load(in);
        return prop;
    }

}
