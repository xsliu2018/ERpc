package top.xsliu.erpc.core.codec;

import top.xsliu.erpc.core.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编码器
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/1:21 上午
 * @author: lxs
 */
public class RpcEncoder<E> extends MessageToByteEncoder<E> {
    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private final Class<E> genericClass;
    private final Serializer serializer;

    public RpcEncoder(Class<E> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, E in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            try {
                byte[] data = serializer.serialize(in);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex) {
                logger.error("Encode error: " + ex.toString());
            }
        }
    }
}
