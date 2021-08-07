package top.xsliu.erpc.core.codec;

import top.xsliu.erpc.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 解码器
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:21 下午
 * @author: lxs
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    private final Class<?> genericClass;
    private final Serializer serializer;

    public RpcDecoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj;
        try {
            obj = serializer.deserialize(data, genericClass);
            out.add(obj);
        } catch (Exception ex) {
            log.error("Decode error: " + ex.getMessage());
        }
    }

}
