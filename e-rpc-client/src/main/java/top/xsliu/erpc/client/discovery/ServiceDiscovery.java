package top.xsliu.erpc.client.discovery;

import top.xsliu.erpc.client.connection.ConnectionManager;
import top.xsliu.erpc.core.config.Constant;
import top.xsliu.erpc.core.protocol.RpcServerInfo;
import top.xsliu.erpc.core.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务发现
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/2:24 上午
 * @author: lxs
 */
@Slf4j
public class ServiceDiscovery {

    private final CuratorClient curatorClient;

    public ServiceDiscovery(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        discoveryService();
    }

    private void discoveryService() {
        try {
            // Get initial service info
            log.info("Get initial services info");
            getServiceAndUpdateServer();
            // Add watch listener
            curatorClient.watchPathChildrenNode(Constant.ZK_REGISTRY_PATH, (curatorFramework, pathChildrenCacheEvent) -> {
                PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                ChildData childData = pathChildrenCacheEvent.getData();
                switch (type) {
                    case CONNECTION_RECONNECTED:
                        log.info("Reconnected to zk, try to get latest service list");
                        getServiceAndUpdateServer();
                        break;
                    case CHILD_ADDED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                    case CHILD_UPDATED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                    case CHILD_REMOVED:
                        getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                        break;
                }
            });
        } catch (Exception ex) {
            log.error("Watch node exception: " + ex.getMessage());
        }
    }

    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(Constant.ZK_REGISTRY_PATH);
            List<RpcServerInfo> dataList = new ArrayList<>();
            for (String node : nodeList) {
                log.debug("Service node: " + node);
                byte[] bytes = curatorClient.getData(Constant.ZK_REGISTRY_PATH + "/" + node);
                String json = new String(bytes);
                RpcServerInfo rpcServerInfo = RpcServerInfo.fromJson(json);
                dataList.add(rpcServerInfo);
            }
            log.debug("Service node data: {}", dataList);
            //Update the service info based on the latest data
            UpdateConnectedServer(dataList);
        } catch (Exception e) {
            log.error("Get node exception: " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        log.info("Child data updated, path:{},type:{},data:{},", path, type, data);
        RpcServerInfo rpcServerInfo =  RpcServerInfo.fromJson(data);
        updateConnectedServer(rpcServerInfo, type);
    }

    private void UpdateConnectedServer(List<RpcServerInfo> dataList) {
        ConnectionManager.getInstance().updateConnectedServer(dataList);
    }


    private void updateConnectedServer(RpcServerInfo rpcServerInfo, PathChildrenCacheEvent.Type type) {
        ConnectionManager.getInstance().updateConnectedServer(rpcServerInfo, type);
    }

    public void stop() {
        this.curatorClient.close();
    }
}
