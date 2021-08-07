package top.xsliu.erpc.client.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:24 下午
 * @author: lxs
 */

public interface SerializableMethod<T> extends Serializable {
    default String getName() throws Exception {
        Method write = this.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) write.invoke(this);
        return serializedLambda.getImplMethodName();
    }
}
