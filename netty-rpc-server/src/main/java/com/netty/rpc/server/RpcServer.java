package com.netty.rpc.server;

import com.netty.rpc.annotation.NettyRpcService;
import com.netty.rpc.server.core.NettyServer;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * RPC服务器
 * 在服务器启动时，从spring容器中获取到带有注解的bean，将这些bean注册到zk上
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4:21 下午
 * @author: lxs
 */
public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean, DisposableBean {
    public RpcServer(String serverAddress, String registryAddress) {
        super(serverAddress, registryAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        // 通过注解获取bean
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(NettyRpcService.class);
        if (MapUtils.isEmpty(serviceBeanMap)) {
            return;
        }
        serviceBeanMap.values().forEach(super::addService);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    @Override
    public void destroy() {
        super.stop();
    }
}
