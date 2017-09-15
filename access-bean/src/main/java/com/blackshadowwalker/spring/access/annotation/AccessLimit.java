package com.blackshadowwalker.spring.access.annotation;

import java.lang.annotation.*;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 * 访问限制
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AccessLimit {

    /**
     * key prefix(default method.name)
     * @return
     */
    String value() default "";

    /**
     * key(SpEL)
     *
     * @return
     */
    String key();

    /**
     * 限制次数([-1, max))
     * -1 : 无限制
     *
     * @return
     */
    int limit() default -1;

    /**
     * 计算频率限制单位时间(秒)
     *
     * @return
     */
    long unitTime() default 24 * 3600;

    /**
     * 异常提示信息
     * @return
     */
    String errorMsg() default "";

    /**
     * 命中规则后是否将'命中访问限制'结果缓存延期
     * @return
     */
    boolean delayExpireOnHit() default false;

    /**
     * 命中访问限制，每次延期过期时间步长(秒)[0, Integer.MAX]
     * @return
     */
    int delayExpireStep() default 0;

}
