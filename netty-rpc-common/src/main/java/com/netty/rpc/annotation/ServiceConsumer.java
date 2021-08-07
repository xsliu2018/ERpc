package com.netty.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 服务消费注解
 * @description: <a href="mailto:xsl2011@outlook.com" />
 * @time: 2021/8/8/1:21 上午
 * @author: lxs
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ServiceConsumer {
    String version() default "";
}