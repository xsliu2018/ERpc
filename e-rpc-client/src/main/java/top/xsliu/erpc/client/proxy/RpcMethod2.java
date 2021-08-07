package top.xsliu.erpc.client.proxy;

@FunctionalInterface
public interface RpcMethod2<T, P1, P2> extends SerializableMethod<T> {
    Object apply(T t, P1 p1, P2 p2);
}
