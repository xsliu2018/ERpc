package com.netty.rpc.server.core;

import com.netty.rpc.annotation.NettyRpcService;
import com.netty.rpc.server.registry.ServiceRegistry;
import com.netty.rpc.util.ServiceUtil;
import com.netty.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Netty服务器，开启服务，用于接收远程连接 在服务器启动时，从spring容器中获取到带有注解的bean，将这些bean注册到zk上
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4:00 下午
 * @author: lxs
 */
@Slf4j
public class NettyServer implements Server {

    private Thread thread;
    private final String serverAddress;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, Object> serviceMap = new HashMap<>();

    public NettyServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ServiceRegistry(registryAddress);
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        log.info(
                "Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }

    public void addService(Object serviceBean) {
        if (serviceBean == null) {
            return;
        }
        NettyRpcService nettyRpcService = serviceBean.getClass().getAnnotation(NettyRpcService.class);
        String interfaceName = nettyRpcService.value().getName();
        String version = nettyRpcService.version();
        addService(interfaceName, version, serviceBean);
    }

    public void start() {
        Runnable r = new Runnable() {
            final ThreadPoolExecutor threadPoolExecutor =
                    ThreadPoolUtil.makeServerThreadPool(NettyServer.class.getSimpleName(), 16, 32);
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap
                            .group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    ChannelFuture future = bootstrap.bind(host, port).sync();

                    serviceRegistry.registerService(host, port, serviceMap);
                    log.info("Server started on port {}", port);
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        log.info("Rpc server remoting server stop");
                    } else {
                        log.error("Rpc server remoting server error", e);
                    }
                } finally {
                    try {
                        serviceRegistry.unregisterService();
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }
        };
        thread = new Thread(r);
        thread.start();
    }

    public void stop() {
        // 销毁掉服务线程
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
