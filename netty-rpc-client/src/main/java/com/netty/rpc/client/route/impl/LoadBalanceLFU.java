package com.netty.rpc.client.route.impl;

import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.protocol.RpcServerInfo;
import com.netty.rpc.protocol.RpcServiceInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LFU负载均衡
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:24 下午
 * @author: lxs
 */
public class LoadBalanceLFU implements LoadBalance {
    private final ConcurrentMap<String, Map<RpcServerInfo, Integer>> jobLfuMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    public RpcServerInfo doRoute(String serviceKey, List<RpcServerInfo> addressList) {
        
    }

    @Override
    public RpcServerInfo route(String serviceKey, Map<RpcServerInfo, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcServerInfo>> serviceMap = initServiceMapping(connectedServerNodes);
        List<RpcServerInfo> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
