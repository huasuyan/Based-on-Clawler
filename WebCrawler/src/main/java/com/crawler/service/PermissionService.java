package com.crawler.service;

import java.util.Map;

public interface PermissionService {
    Map<String, Object> getAuthority(Long userId);
    void evictCache(Long userId);
}