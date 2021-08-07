package top.xsliu.erpc.core.util;

/**
 * 服务工具类
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/2:21 下午
 * @author: lxs
 */
public class ServiceUtil {
    public static final String SERVICE_CONCAT_TOKEN = "#";

    public static String generateServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if (version != null && version.trim().length() > 0) {
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }
}
