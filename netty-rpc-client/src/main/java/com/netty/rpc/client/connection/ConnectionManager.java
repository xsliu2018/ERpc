package com.netty.rpc.client.connection;

import com.netty.rpc.client.handler.RpcClientHandler;
import com.netty.rpc.client.handler.RpcClientInitializer;
import com.netty.rpc.client.route.LoadBalance;
import com.netty.rpc.client.route.impl.LoadBalanceRoundRobin;
import com.netty.rpc.protocol.RpcServerInfo;
import com.netty.rpc.protocol.RpcServiceInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 连接管理器
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:24 下午
 * @author: lxs
 */
@Slf4j
public class ConnectionManager {
    private static final long ALIVE_TIME = 600L;
    private static final int WAIT_TIME = 6000;
    private static final int QUEUE_SIZE = 10000;

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8,
            ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(QUEUE_SIZE));

    private final Map<RpcServerInfo, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<RpcServerInfo> rpcServerInfoSet = new CopyOnWriteArraySet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition connected = lock.newCondition();
    private final LoadBalance loadBalance = new LoadBalanceRoundRobin();
    private volatile boolean isRunning = true;

    private ConnectionManager() {
    }

    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    public void updateConnectedServer(List<RpcServerInfo> serviceList) {
        // Now using 2 collections to manage the service info and TCP connections because making the connection is async
        // Once service info is updated on ZK, will trigger this function
        // Actually client should only care about the service it is using
        if (serviceList != null && serviceList.size() > 0) {
            // Update local server nodes cache
            HashSet<RpcServerInfo> serviceSet = new HashSet<>(serviceList.size());
            serviceSet.addAll(serviceList);

            // Add new server info
            for (final RpcServerInfo rpcServerInfo : serviceSet) {
                if (!rpcServerInfoSet.contains(rpcServerInfo)) {
                    connectServerNode(rpcServerInfo);
                }
            }

            // Close and remove invalid server nodes
            for (RpcServerInfo rpcServerInfo : rpcServerInfoSet) {
                if (!serviceSet.contains(rpcServerInfo)) {
                    log.info("Remove invalid service: " + rpcServerInfo.toJson());
                    removeAndCloseHandler(rpcServerInfo);
                }
            }
        } else {
            // No available service
            log.error("No available service!");
            for (RpcServerInfo rpcServerInfo : rpcServerInfoSet) {
                removeAndCloseHandler(rpcServerInfo);
            }
        }
    }


    public void updateConnectedServer(RpcServerInfo rpcServerInfo, PathChildrenCacheEvent.Type type) {
        if (rpcServerInfo == null) {
            return;
        }
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcServerInfoSet.contains(rpcServerInfo)) {
            connectServerNode(rpcServerInfo);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            //TODO We may don't need to reconnect remote server if the server'IP and server'port are not changed
            removeAndCloseHandler(rpcServerInfo);
            connectServerNode(rpcServerInfo);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(rpcServerInfo);
        } else {
            throw new IllegalArgumentException("Unknown param type:" + type);
        }
    }

    private void connectServerNode(RpcServerInfo rpcServerInfo) {
        if (rpcServerInfo.getServiceInfoList() == null || rpcServerInfo.getServiceInfoList().isEmpty()) {
            log.info("No service on node, host: {}, port: {}", rpcServerInfo.getHost(), rpcServerInfo.getPort());
            return;
        }
        rpcServerInfoSet.add(rpcServerInfo);
        log.info("New service node, host: {}, port: {}", rpcServerInfo.getHost(), rpcServerInfo.getPort());
        for (RpcServiceInfo serviceProtocol : rpcServerInfo.getServiceInfoList()) {
            log.info("New service info, name: {}, version: {}", serviceProtocol.getServiceName(), serviceProtocol.getVersion());
        }
        final InetSocketAddress remotePeer = new InetSocketAddress(rpcServerInfo.getHost(), rpcServerInfo.getPort());
        threadPoolExecutor.submit(() -> {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer());

            ChannelFuture channelFuture = b.connect(remotePeer);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()) {
                    log.info("Successfully connect to remote server, remote peer = " + remotePeer);
                    RpcClientHandler handler = channelFuture1.channel().pipeline().get(RpcClientHandler.class);
                    connectedServerNodes.put(rpcServerInfo, handler);
                    handler.setRpcProtocol(rpcServerInfo);
                    signalAvailableHandler();
                } else {
                    log.error("Can not connect to remote server, remote peer = " + remotePeer);
                }
            });
        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            log.warn("Waiting for available service");
            boolean await = connected.await(WAIT_TIME, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                log.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcServerInfo rpcServerInfo = loadBalance.route(serviceKey, connectedServerNodes);
        RpcClientHandler handler = connectedServerNodes.get(rpcServerInfo);
        if (handler != null) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    private void removeAndCloseHandler(RpcServerInfo rpcServerInfo) {
        RpcClientHandler handler = connectedServerNodes.get(rpcServerInfo);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(rpcServerInfo);
        rpcServerInfoSet.remove(rpcServerInfo);
    }

    public void removeHandler(RpcServerInfo rpcServerInfo) {
        rpcServerInfoSet.remove(rpcServerInfo);
        connectedServerNodes.remove(rpcServerInfo);
        log.info("Remove one connection, host: {}, port: {}", rpcServerInfo.getHost(), rpcServerInfo.getPort());
    }

    public void stop() {
        isRunning = false;
        for (RpcServerInfo rpcServerInfo : rpcServerInfoSet) {
            removeAndCloseHandler(rpcServerInfo);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}

