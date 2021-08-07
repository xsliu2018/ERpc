package com.netty.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务注解
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/7/3:21 下午
 * @author: lxs
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface NettyRpcService {
    Class<?> value();

    String version() default "";

    int timeWait() default 10000;
}
