package com.crawler.entity.dto.role;

import com.crawler.entity.Role;
import com.crawler.util.CommonUtil;
import lombok.Data;

import java.util.Date;

// 列表/详情返回DTO，负责拼装 dataScopeText、statusText
@Data
public class RoleListDto {

    private Long    roleId;
    private String  roleName;
    private Integer dataScope;
    private String  dataScopeText;   // 后端解析，不存库
    private String  remark;
    private Integer status;
    private String  statusText;      // 后端解析，不存库
    private Object  authority;       // JSON反序列化为对象返回前端
    private Date    createTime;
    private Date    updateTime;
    private String  operateTime;     // 格式化后的updateTime字符串（列表用）

    public RoleListDto(Role role) {
        this.roleId        = role.getRoleId();
        this.roleName      = role.getRoleName();
        this.dataScope     = role.getDataScope();
        this.dataScopeText = parseDataScope(role.getDataScope());
        this.remark        = role.getRemark();
        this.status        = role.getStatus();
        this.statusText    = role.getStatus() != null && role.getStatus() == 1 ? "启用" : "禁用";
        this.authority     = CommonUtil.parseJson(role.getAuthority());
        this.createTime    = role.getCreateTime();
        this.updateTime    = role.getUpdateTime();
        // 列表字段 operateTime 取 updateTime 格式化
        if (role.getUpdateTime() != null) {
            this.operateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(role.getUpdateTime());
        }
    }

    private static String parseDataScope(Integer dataScope) {
        if (dataScope == null) return "";
        switch (dataScope) {
            case 1:  return "本处室";
            case 2:  return "本单位";
            case 3:  return "本系统";
            default: return "";
        }
    }
}