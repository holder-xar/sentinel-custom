package com.xj.sentinel.cluster.server;

import com.xj.sentinel.cluster.server.util.SentinelServer;

/**
 * @author: holder
 * @creat: 2019/8/30
 * @Description:
 */
public class SentinelServerApplication {

    public static void main(String[] args) throws Exception {

        SentinelServer sentinelServer = new SentinelServer();

        sentinelServer.init();
        sentinelServer.start();

    }
}
