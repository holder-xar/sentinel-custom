package com.xj.sentinel.util;

import java.io.File;

/**
 * @author holder
 * @date 2019/6/12
 * @Description:
 */
public class ZookeeperConfigUtils {

    public static final String GROUP_ID = "SENTINEL_GROUP";

    public static final String FLOW_RULE_DATA_ID_POSTFIX = "-flow-rules";

    private static final String FLOW_PARAM_RULE_DATA_ID_POSTFIX = "-param-flow-rules";

    private static final String CLUSTER_CONFIG_DATA_ID_POSTFIX = "-cluster-client-config";

    private ZookeeperConfigUtils(){}

    /**
     *
     * @param app name
     * @return zk path
     */
    public static String getFlowRuleZkPath(String app){
        return File.separator + GROUP_ID + File.separator + app + FLOW_RULE_DATA_ID_POSTFIX;
    }

    /**
     *
     * @param app name
     * @return zk param rule path
     */
    public static String getFlowParamRuleZkPath(String app){
        return File.separator + GROUP_ID + File.separator + app + FLOW_PARAM_RULE_DATA_ID_POSTFIX;
    }

    /**
     *
     * @param app name
     * @return cluster configDataId
     */
    public static String getClusterConfigDataId(String app){
        return app + CLUSTER_CONFIG_DATA_ID_POSTFIX;
    }


}
