package com.blackshadowwalker.spring.access.annotation;

import java.lang.annotation.*;

/**
 * Author : LI-JIAN
 * Date   : 2017-07-17
 * 访问限制组合
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AccessLimits {

    AccessLimit[] value() default {};

}
