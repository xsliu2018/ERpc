package com.app.test.util;



import com.app.test.service.HelloServiceImpl;
import com.app.test.service.Person;
import top.xsliu.erpc.core.codec.RpcRequest;
import top.xsliu.erpc.core.codec.RpcResponse;
import top.xsliu.erpc.core.util.JSONUtil;
import top.xsliu.erpc.core.util.SerializationUtil;

import java.util.UUID;

/**
 * Created by jsc on 2016-03-10.
 */
public class JsonTest {
    public static void main(String[] args) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(UUID.randomUUID().toString());
        response.setError("Error msg");
        System.out.println(response.getRequestId());

        byte[] datas = JSONUtil.serialize(response);
        System.out.println("Json byte length: " + datas.length);

        byte[] datas2 = SerializationUtil.serialize(response);
        System.out.println("Protobuf byte length: " + datas2.length);

        RpcResponse resp = (RpcResponse) JSONUtil.deserialize(datas, RpcResponse.class);
        System.out.println(resp.getRequestId());
    }


    private static void TestJsonSerialize() {
        RpcRequest request = new RpcRequest();
        request.setClassName(HelloServiceImpl.class.getName());
        request.setMethodName(HelloServiceImpl.class.getDeclaredMethods()[0].getName());
        Person person = new Person("lu", "xiaoxun");
        request.setParameters(new Object[]{person});
        request.setRequestId(UUID.randomUUID().toString());
        System.out.println(request.getRequestId());

        byte[] datas = JSONUtil.serialize(request);
        System.out.println("Json byte length: " + datas.length);

        byte[] datas2 = SerializationUtil.serialize(request);
        System.out.println("Protobuf byte length: " + datas2.length);

        RpcRequest req = (RpcRequest) JSONUtil.deserialize(datas, RpcRequest.class);
        System.out.println(req.getRequestId());
    }

}
