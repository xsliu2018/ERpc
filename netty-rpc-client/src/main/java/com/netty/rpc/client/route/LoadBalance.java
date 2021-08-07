package com.netty.rpc.client.route;

import cn.hutool.core.collection.CollectionUtil;
import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.protocol.RpcServerInfo;
import com.netty.rpc.protocol.RpcServiceInfo;
import com.netty.rpc.util.ServiceUtil;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡接口
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/5:06 下午
 * @author: lxs
 */
public interface LoadBalance {
    /**
     * 初始化服务信息，这个信息来自于zk
     * @param connectedServerNodes 从zk中获取的node节点，map形式，保存着服务信息
     */
    default Map<String, List<RpcServerInfo>> initServiceMapping(Map<RpcServerInfo, RpcClientHandler> connectedServerNodes) {
        Map<String, List<RpcServerInfo>> serviceMap = new HashedMap<>();
        if (CollectionUtil.isNotEmpty(connectedServerNodes)) {
            for (RpcServerInfo rpcServerInfo : connectedServerNodes.keySet()) {
                for (RpcServiceInfo serviceInfo : rpcServerInfo.getServiceInfoList()) {
                    String serviceKey = ServiceUtil.generateServiceKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                    List<RpcServerInfo> rpcServerInfoList = serviceMap.get(serviceKey);
                    if (rpcServerInfoList == null) {
                        rpcServerInfoList = new ArrayList<>();
                    }
                    rpcServerInfoList.add(rpcServerInfo);
                    serviceMap.putIfAbsent(serviceKey, rpcServerInfoList);
                }
            }
        }
        return serviceMap;
    }

    RpcServerInfo route(String serviceKey, Map<RpcServerInfo, RpcClientHandler> connectedServerNodes) throws Exception;
}

