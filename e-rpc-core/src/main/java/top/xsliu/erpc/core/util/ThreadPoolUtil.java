package top.xsliu.erpc.core.util;

import java.util.concurrent.*;

/**
 * 线程工具类
 *
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/2:21 下午
 * @author: lxs
 */
public class ThreadPoolUtil {
    public static ThreadPoolExecutor makeServerThreadPool(final String serviceName, int corePoolSize, int maxPoolSize) {

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> new Thread(r, "e-rpc-" + serviceName + "-" + r.hashCode()),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
