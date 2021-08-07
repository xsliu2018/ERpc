package top.xsliu.erpc.core.protocol;

import top.xsliu.erpc.core.util.JSONUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务数据，包含名称，版本等
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/4:21 下午
 * @author: lxs
 */
@Data
public class RpcServiceInfo implements Serializable {
    private String serviceName;
    private String version;
    private int waitTime;
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }

    public String toJson() {
        return JSONUtil.objectToJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
