package cn.cat.middleware.dbrouter.annotation;

import java.lang.annotation.*;

/**
 * 分表注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterSplit {
    boolean splitTable() default false;
}
