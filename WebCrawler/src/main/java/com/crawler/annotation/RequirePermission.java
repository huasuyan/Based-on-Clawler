package com.crawler.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String module();   // 模块名，如 "alert"、"role"
    String action();   // 操作名，如 "alert_select"、"role_delete"
}