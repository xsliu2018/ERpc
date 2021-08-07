package top.xsliu.erpc.core.protocol;

import top.xsliu.erpc.core.util.JSONUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * rpc服务数据,ip，端口以及提供的服务列表
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/5:06 下午
 * @author: lxs
 */
@Data
public class RpcServerInfo implements Serializable {
    private static final long serialVersionUID = -1102180003395190700L;
    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfoList;

    public String toJson() {
        return JSONUtil.objectToJson(this);
    }

    public static RpcServerInfo fromJson(String json) {
        return JSONUtil.jsonToObject(json, RpcServerInfo.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServerInfo that = (RpcServerInfo) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                isListEquals(serviceInfoList, that.getServiceInfoList());
    }

    private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
        if (thisList == null && thatList == null) {
            return true;
        }
        if (thisList == null || thatList == null || thisList.size() != thatList.size()) {
            return false;
        }
        return thisList.containsAll(thatList) && thatList.containsAll(thisList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList.hashCode());
    }

    @Override
    public String toString() {
        return toJson();
    }

}
