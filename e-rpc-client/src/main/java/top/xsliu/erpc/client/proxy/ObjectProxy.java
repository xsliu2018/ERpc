package top.xsliu.erpc.client.proxy;

import top.xsliu.erpc.client.connection.ConnectionManager;
import top.xsliu.erpc.client.handler.RpcClientHandler;
import top.xsliu.erpc.client.handler.RpcFuture;
import top.xsliu.erpc.core.codec.RpcRequest;
import top.xsliu.erpc.core.util.ServiceUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 代理
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/6/11:24 下午
 * @author: lxs
 */
@Slf4j
public class ObjectProxy<T>
    implements InvocationHandler, RpcService<T, SerializableMethod<T>> {
  private final Class<T> clazz;
  private final String version;

  public ObjectProxy(Class<T> clazz, String version) {
    this.clazz = clazz;
    this.version = version;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class == method.getDeclaringClass()) {
      String name = method.getName();
      switch (name) {
        case "equals":
          return proxy == args[0];
        case "hashCode":
          return System.identityHashCode(proxy);
        case "toString":
          return proxy.getClass().getName()
              + "@"
              + Integer.toHexString(System.identityHashCode(proxy))
              + ", with InvocationHandler "
              + this;
        default:
          throw new IllegalStateException(String.valueOf(method));
      }
    }

    RpcRequest request = new RpcRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setClassName(method.getDeclaringClass().getName());
    request.setMethodName(method.getName());
    request.setParameterTypes(method.getParameterTypes());
    request.setParameters(args);
    request.setVersion(version);
    // Debug
    if (log.isDebugEnabled()) {
      log.debug(method.getDeclaringClass().getName());
      log.debug(method.getName());
      for (int i = 0; i < method.getParameterTypes().length; ++i) {
        log.debug(method.getParameterTypes()[i].getName());
      }
      for (Object arg : args) {
        log.debug(arg.toString());
      }
    }

    String serviceKey = ServiceUtil.generateServiceKey(method.getDeclaringClass().getName(), version);
    RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
    RpcFuture rpcFuture = handler.sendRequest(request);
    return rpcFuture.get();
  }

  @Override
  public RpcFuture call(String funcName, Object... args) throws Exception {
    String serviceKey = ServiceUtil.generateServiceKey(this.clazz.getName(), version);
    RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
    RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
    return handler.sendRequest(request);
  }

  @Override
  public RpcFuture call(SerializableMethod<T> tSerializableMethod, Object... args)
      throws Exception {
    String serviceKey = ServiceUtil.generateServiceKey(this.clazz.getName(), version);
    RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
    RpcRequest request = createRequest(this.clazz.getName(), tSerializableMethod.getName(), args);
    return handler.sendRequest(request);
  }

  private RpcRequest createRequest(String className, String methodName, Object[] args) {
    RpcRequest request = new RpcRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setClassName(className);
    request.setMethodName(methodName);
    request.setParameters(args);
    request.setVersion(version);
    Class[] parameterTypes = new Class[args.length];
    // Get the right class type
    for (int i = 0; i < args.length; i++) {
      parameterTypes[i] = args[i].getClass();
    }
    request.setParameterTypes(parameterTypes);

    // Debug
    if (log.isDebugEnabled()) {
      log.debug(className);
      log.debug(methodName);
      for (Class parameterType : parameterTypes) {
        log.debug(parameterType.getName());
      }
      for (Object arg : args) {
        log.debug(arg.toString());
      }
    }

    return request;
  }
}
