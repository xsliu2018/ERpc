package com.netty.rpc.client.route.impl;

import com.google.common.hash.Hashing;
import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.protocol.RpcServerInfo;

import java.util.List;
import java.util.Map;

/**
 * 一致性hash负载均衡实现
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/6:21 下午
 * @author: lxs
 */
public class LoadBalanceConsistentHash implements LoadBalance {

    public RpcServerInfo doRoute(String serviceKey, List<RpcServerInfo> addressList) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
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
