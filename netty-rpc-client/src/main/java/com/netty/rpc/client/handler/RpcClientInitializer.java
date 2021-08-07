package com.netty.rpc.client.handler;

import com.netty.rpc.codec.*;
import com.netty.rpc.serializer.Serializer;
import com.netty.rpc.serializer.hessian.HessianSerializer;
import com.netty.rpc.serializer.kryo.KryoSerializer;
import com.netty.rpc.serializer.protostuff.ProtostuffSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * client channel 初始化
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:21 下午
 * @author: lxs
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final int FRAME_SIZE = 2 ^ 16;
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        Serializer serializer = KryoSerializer.class.getConstructor().newInstance();
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new RpcEncoder(RpcRequest.class, serializer));
        cp.addLast(new LengthFieldBasedFrameDecoder(FRAME_SIZE, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast(new RpcClientHandler());
    }
}
