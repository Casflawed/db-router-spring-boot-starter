package com.flameking.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * 路由注解，携带路由算法入参
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DBRouter {

    String param() default "";

}
