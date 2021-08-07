package com.netty.rpc.client.route.impl;

import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.protocol.RpcServerInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LRU负载均衡
 *
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:24 下午
 * @author: lxs
 */
public class LoadBalanceLRU implements LoadBalance {
  private final ConcurrentMap<String, LinkedHashMap<RpcServerInfo, RpcServerInfo>> jobLRUMap =
      new ConcurrentHashMap<>();
  private long CACHE_VALID_TIME = 0;

  public RpcServerInfo doRoute(String serviceKey, List<RpcServerInfo> addressList) {
    // cache clear
    if (System.currentTimeMillis() > CACHE_VALID_TIME) {
      jobLRUMap.clear();
      CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
    }

    // init lru
    LinkedHashMap<RpcServerInfo, RpcServerInfo> lruHashMap = jobLRUMap.get(serviceKey);
    if (lruHashMap == null) {
      /*
       * LinkedHashMap
       * a、accessOrder：ture=访问顺序排序（get/put时排序）/ACCESS-LAST；false=插入顺序排期/FIFO；
       * b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；
       *      可封装LinkedHashMap并重写该方法，比如定义最大容量，超出是返回true即可实现固定长度的LRU算法；
       */
      lruHashMap =
          new LinkedHashMap<RpcServerInfo, RpcServerInfo>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<RpcServerInfo, RpcServerInfo> eldest) {
              return super.size() > 1000;
            }
          };
      jobLRUMap.putIfAbsent(serviceKey, lruHashMap);
    }

    // put new
    for (RpcServerInfo address : addressList) {
      if (!lruHashMap.containsKey(address)) {
        lruHashMap.put(address, address);
      }
    }
    // remove old
    List<RpcServerInfo> delKeys = new ArrayList<>();
    for (RpcServerInfo existKey : lruHashMap.keySet()) {
      if (!addressList.contains(existKey)) {
        delKeys.add(existKey);
      }
    }
    if (delKeys.size() > 0) {
      for (RpcServerInfo delKey : delKeys) {
        lruHashMap.remove(delKey);
      }
    }

    // load
    RpcServerInfo eldestKey = lruHashMap.entrySet().iterator().next().getKey();
    return lruHashMap.get(eldestKey);
  }

  @Override
  public RpcServerInfo route(
      String serviceKey, Map<RpcServerInfo, RpcClientHandler> connectedServerNodes) throws Exception {
    Map<String, List<RpcServerInfo>> serviceMap = initServiceMapping(connectedServerNodes);
    List<RpcServerInfo> addressList = serviceMap.get(serviceKey);
    if (addressList != null && addressList.size() > 0) {
      return doRoute(serviceKey, addressList);
    } else {
      throw new Exception("Can not find connection for service: " + serviceKey);
    }
  }
}
