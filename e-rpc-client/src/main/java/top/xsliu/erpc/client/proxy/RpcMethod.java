package top.xsliu.erpc.client.proxy;

/**
 * lambda method reference
 * g-yu
 */
@FunctionalInterface
public interface RpcMethod<T, P> extends SerializableMethod<T> {
    Object apply(T t, P p);
}
