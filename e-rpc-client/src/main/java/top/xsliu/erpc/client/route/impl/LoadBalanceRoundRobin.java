package top.xsliu.erpc.client.route.impl;

import top.xsliu.erpc.client.handler.RpcClientHandler;
import top.xsliu.erpc.client.route.LoadBalance;
import top.xsliu.erpc.core.protocol.RpcServerInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训负载均衡实现
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/0:21 上午
 * @author: lxs
 */
public class LoadBalanceRoundRobin implements LoadBalance {
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public RpcServerInfo doRoute(List<RpcServerInfo> addressList) {
        int size = addressList.size();
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
