package com.crawler.config;

import com.crawler.annotation.RequirePermission;
import com.crawler.entity.User;
import com.crawler.exception.PermissionDeniedException;
import com.crawler.service.PermissionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Resource private PermissionService permissionService;

    @Before("@annotation(rp)")
    public void checkPermission(RequirePermission rp) {
        // 1. 从 RequestContextHolder 拿 request（无需注入 HttpServletRequest）
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new PermissionDeniedException("无法获取请求上下文");
        HttpServletRequest request = attrs.getRequest();

        // 2. 取出当前用户（JwtInterceptor 已经写入）
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null) throw new PermissionDeniedException("未登录，请先登录");

        // 3. 获取该用户聚合后的 authority（走 Redis 缓存）
        Map<String, Object> authority = permissionService.getAuthority(
                Long.valueOf(currentUser.getUserId()));

        // 4. 检查 module 层
        String module = rp.module();
        String action = rp.action();
        Object moduleObj = authority.get(module);
        if (!(moduleObj instanceof Map)) {
            throw new PermissionDeniedException("权限不足：无 [" + module + "] 模块权限");
        }

        // 5. 检查 action 层
        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) moduleObj;
        Object val = moduleMap.get(action);
        if (val == null || Integer.parseInt(val.toString()) != 1) {
            log.warn("[权限拦截] userId={} module={} action={} 无权限",
                    currentUser.getUserId(), module, action);
            throw new PermissionDeniedException("权限不足：无 [" + action + "] 操作权限");
        }

        log.debug("[权限通过] userId={} module={} action={}",
                currentUser.getUserId(), module, action);
    }
}