package com.netty.rpc.client.route.impl;

import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.protocol.RpcServerInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round robin load balance
 * Created by luxiaoxun on 2020-08-01.
 */
public class LoadBalanceRoundRobin implements LoadBalance {
    private AtomicInteger roundRobin = new AtomicInteger(0);

    public RpcServerInfo doRoute(List<RpcServerInfo> addressList) {
        int size = addressList.size();
        // Round robin
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
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
