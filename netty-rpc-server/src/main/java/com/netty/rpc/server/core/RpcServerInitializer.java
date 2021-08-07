package com.netty.rpc.server.core;

import com.netty.rpc.codec.*;
import com.netty.rpc.serializer.Serializer;
import com.netty.rpc.serializer.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty channel初始化
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4:21 下午
 * @author: lxs
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final int FRAME_SIZE = 2 ^ 16;
    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor threadPoolExecutor;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        Serializer serializer = KryoSerializer.class.getConstructor().newInstance();
        ChannelPipeline pipeline = channel.pipeline();
        // 责任链
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(FRAME_SIZE, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));
        pipeline.addLast(new RpcEncoder(RpcResponse.class, serializer));
        pipeline.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
    }
}
