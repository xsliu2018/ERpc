package top.xsliu.erpc.core.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务响应
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/1:25 上午
 * @author: lxs
 */
@Data
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 8215493329459772524L;

    private String requestId;
    private String error;
    private Object result;
}
