package com.app.test.service;


import top.xsliu.erpc.core.annotation.ServiceConsumer;

public class FooService implements Foo {
    @ServiceConsumer(version = "1.0")
    private HelloService helloService1;

    @ServiceConsumer(version = "2.0")
    private HelloService helloService2;

    @Override
    public String say(String s) {
        return helloService1.hello(s);
    }
}
