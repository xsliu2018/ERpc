package com.netty.rpc.protocol;

import com.netty.rpc.util.JsonUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

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
        return JsonUtil.objectToJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
