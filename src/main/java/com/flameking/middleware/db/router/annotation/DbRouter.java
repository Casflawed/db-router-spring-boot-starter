package com.flameking.middleware.db.router.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DbRouter {
    String key() default "";
}
