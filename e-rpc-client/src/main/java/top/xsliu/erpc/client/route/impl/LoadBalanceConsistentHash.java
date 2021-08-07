package top.xsliu.erpc.client.route.impl;

import com.google.common.hash.Hashing;
import top.xsliu.erpc.client.handler.RpcClientHandler;
import top.xsliu.erpc.client.route.LoadBalance;
import top.xsliu.erpc.core.protocol.RpcServerInfo;

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
