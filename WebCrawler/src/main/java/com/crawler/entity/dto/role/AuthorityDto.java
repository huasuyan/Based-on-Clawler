package com.crawler.entity.dto.role;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuthorityDto {
    Map<String, Object> authority;

    public AuthorityDto() {
        Map<String, String> alert = new HashMap<>();
        alert.put("alert_select", "查询预警设置");
        alert.put("alert_insert", "创建预警设置");
        alert.put("alert_update", "更新预警设置");
        alert.put("alert_delete", "删除预警设置");
        Map<String, String> report = new HashMap<>();
        report.put("report_select", "查询报告专题");
        report.put("report_update", "更新报告专题");
        report.put("report_insert", "创建报告专题");
        report.put("report_delete", "删除报告专题");
        Map<String, String> role = new HashMap<>();
        role.put("role_insert", "创建角色");
        role.put("role_update", "更新角色");
        role.put("role_delete", "删除角色");
        Map<String, String> dept = new HashMap<>();
        dept.put("dept_insert", "创建部门");
        dept.put("dept_update", "更新部门");
        dept.put("dept_delete", "删除部门");
        Map<String, String> dept_user = new HashMap<>();
        dept_user.put("dept_user_select", "查询部门用户");
        dept_user.put("dept_user_insert", "创建部门用户");
        dept_user.put("dept_user_update", "更新部门用户");
        dept_user.put("dept_user_delete", "删除部门用户");
        Map<String, String> case_ = new HashMap<>();
        case_.put("case_select", "查询办件");
        case_.put("case_insert", "创建办件");
        case_.put("case_update", "更新办件");
        case_.put("case_delete", "删除办件");



        authority = new HashMap<>();
        authority.put("role", role);
        authority.put("alert", alert);
        authority.put("report", report);
        authority.put("dept", dept);
        authority.put("dept_user", dept_user);
        authority.put("case", case_);
    }
}
