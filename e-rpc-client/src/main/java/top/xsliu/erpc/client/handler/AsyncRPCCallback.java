package top.xsliu.erpc.client.handler;

/**
 * 异步回调接口
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4.57 下午
 * @author: lxs
 */
public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);

}
