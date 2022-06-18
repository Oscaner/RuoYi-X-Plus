package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限组
 *
 * @author weibocy
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    DataColumn[] value();

}
