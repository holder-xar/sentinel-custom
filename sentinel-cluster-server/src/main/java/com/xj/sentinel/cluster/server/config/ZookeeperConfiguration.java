package com.xj.sentinel.cluster.server.config;

/**
 * @author: holder
 * @creat: 2019/8/30
 * @Description:
 */
public class ZookeeperConfiguration {


    private String projectName;
    private String dashboardServer;
    private String zookeeperAddress;

    public String getProjectName() {
        return projectName;
    }

    public ZookeeperConfiguration setProjectName(String projectName) {
        this.projectName = projectName;
        System.setProperty("project.name", projectName);
        return this;
    }

    public String getDashboardServer() {
        return dashboardServer;
    }

    public ZookeeperConfiguration setDashboardServer(String dashboardServer) {
        this.dashboardServer = dashboardServer;
        System.setProperty("csp.sentinel.dashboard.server", dashboardServer);
        return this;
    }

    public String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public ZookeeperConfiguration setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
        return this;
    }
}
