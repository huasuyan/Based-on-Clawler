package com.crawler.entity.dto.role;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuthorityDto {
    Map<String, Object> authority;

    public AuthorityDto() {
        Map<String, String> role = new HashMap<>();
        role.put("role_select", "查询角色");
        role.put("role_create", "创建角色");
        role.put("role_update", "更新角色");
        role.put("role_delete", "删除角色");
        Map<String, String> alert = new HashMap<>();
        alert.put("alert_select", "查询预警设置");
        alert.put("alert_create", "创建预警设置");
        alert.put("alert_update", "更新预警设置");
        alert.put("alert_delete", "删除预警设置");
        alert.put("info_list", "查询预警记录");
        alert.put("info_delete", "删除预警记录");
        authority = new HashMap<>();
        authority.put("role", role);
        authority.put("alert", alert);
    }
}
