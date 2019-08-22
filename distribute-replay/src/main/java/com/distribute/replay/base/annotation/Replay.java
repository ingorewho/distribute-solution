package com.distribute.replay.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author renzhiqiang
 * @Description 重复请求注解
 * @Date 2019-08-21
 **/
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Replay {
    /**
     * 是否支持重复请求
     *
     * @return
     */
    boolean enable() default false;

    /**
     * redis中请求key过期时间
     * @return
     */
    long expireTime();
}
