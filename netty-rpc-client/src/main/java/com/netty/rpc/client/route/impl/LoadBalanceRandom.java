package com.netty.rpc.client.route.impl;

import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.protocol.RpcServerInfo;

import java.util.*;

/**
 * 随机选择的负载均衡实现
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/0:37 上午
 * @author: lxs
 */
public class LoadBalanceRandom implements LoadBalance {
    private final Random random = new Random();

    public RpcServerInfo doRoute(List<RpcServerInfo> addressList) {
        int size = addressList.size();
        // Random
        return addressList.get(random.nextInt(size));
    }

    @Override
    public RpcServerInfo route(String serviceKey, Map<RpcServerInfo, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcServerInfo>> serviceMap = initServiceMapping(connectedServerNodes);
        List<RpcServerInfo> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
