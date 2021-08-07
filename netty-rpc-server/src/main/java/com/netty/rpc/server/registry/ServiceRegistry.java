package com.netty.rpc.server.registry;

import com.netty.rpc.config.Constant;
import com.netty.rpc.protocol.RpcServerInfo;
import com.netty.rpc.protocol.RpcServiceInfo;
import com.netty.rpc.util.ServiceUtil;
import com.netty.rpc.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.state.ConnectionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务注册
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4:21 下午
 * @author: lxs
 */
@Slf4j
public class ServiceRegistry {

    private final CuratorClient curatorClient;
    private final List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress, 5000);
    }

    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        // 注册服务信息
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if (serviceInfo.length >= 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    // 默认版本为1.0.0
                    rpcServiceInfo.setVersion("1.0.0");
                }
                log.info("Register new service: {} ", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                log.warn("Can not get service name and version: {} ", key);
            }
        }
        try {
            RpcServerInfo rpcServerInfo = new RpcServerInfo();
            rpcServerInfo.setHost(host);
            rpcServerInfo.setPort(port);
            rpcServerInfo.setServiceInfoList(serviceInfoList);
            String serviceData = rpcServerInfo.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = Constant.ZK_DATA_PATH + "-" + rpcServerInfo.hashCode();
            path = this.curatorClient.createPathData(path, bytes);
            pathList.add(path);
            log.info("Register {} new service, host: {}, port: {}", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            log.error("Register service fail, exception: {}", e.getMessage());
        }

        curatorClient.addConnectionStateListener((curatorFramework, connectionState) -> {
            if (connectionState == ConnectionState.RECONNECTED) {
                log.info("Connection state: {}, register service after reconnected", connectionState);
                registerService(host, port, serviceMap);
            }
        });
    }

    public void unregisterService() {
        log.info("Unregister all service");
        for (String path : pathList) {
            try {
                this.curatorClient.deletePath(path);
            } catch (Exception ex) {
                log.error("Delete service path error: " + ex.getMessage());
            }
        }
        this.curatorClient.close();
    }
}
