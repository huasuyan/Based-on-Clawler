package com.crawler.service.impl;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.Role;
import com.crawler.mapper.RoleMapper;
import com.crawler.mapper.UserRoleMapper;
import com.crawler.service.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final String PERM_KEY_PREFIX = "perm:user:";
    private static final long   PERM_TTL_SECONDS = 1800L; // 30 分钟

    @Resource private RedisTemplate<String, Object> redisTemplate;
    @Resource private RoleMapper roleMapper;
    @Resource private UserRoleMapper userRoleMapper;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAuthority(Long userId) {
        String key = PERM_KEY_PREFIX + userId;

        // 1. 先查 Redis
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Map) {
            return (Map<String, Object>) cached;
        }

        // 2. 未命中，查数据库并聚合
        Map<String, Object> merged = buildAuthority(userId);

        // 3. 写入 Redis
        redisTemplate.opsForValue().set(key, merged, PERM_TTL_SECONDS, TimeUnit.SECONDS);
        return merged;
    }

    @Override
    public void evictCache(Long userId) {
        redisTemplate.delete(PERM_KEY_PREFIX + userId);
    }

    private Map<String, Object> buildAuthority(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        Map<String, Object> merged = new HashMap<>();
        if (roleIds == null || roleIds.isEmpty()) return merged;

        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role == null || role.getStatus() == null || role.getStatus() == 0) continue;
            if (role.getAuthority() == null) continue;

            Map<String, Object> authMap = JSONUtil.parseObj(role.getAuthority());
            // authority 结构: { "alert": { "alert_select": 1, ... }, "role": {...} }
            for (Map.Entry<String, Object> moduleEntry : authMap.entrySet()) {
                String module = moduleEntry.getKey();
                Object moduleVal = moduleEntry.getValue();
                if (!(moduleVal instanceof Map)) continue;

                Map<String, Object> moduleActions = (Map<String, Object>) moduleVal;
                Map<String, Object> existingModule = (Map<String, Object>)
                        merged.computeIfAbsent(module, k -> new HashMap<String, Object>());

                for (Map.Entry<String, Object> actionEntry : moduleActions.entrySet()) {
                    String action = actionEntry.getKey();
                    int val = parseIntVal(actionEntry.getValue());
                    int prev = parseIntVal(existingModule.get(action));
                    // 多角色取最大值：有 1 就是有权限
                    existingModule.put(action, Math.max(val, prev));
                }
            }
        }
        return merged;
    }

    private int parseIntVal(Object v) {
        if (v == null) return 0;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}