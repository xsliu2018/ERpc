package top.xsliu.erpc.client.proxy;

import top.xsliu.erpc.client.handler.RpcFuture;

/**
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:24 下午
 * @author: lxs
 */
public interface RpcService<T, FN extends SerializableMethod<T>> {
    RpcFuture call(String funcName, Object... args) throws Exception;

    /**
     * lambda method reference
     */
    RpcFuture call(FN fn, Object... args) throws Exception;

}