package com.crawler.entity.dto.role;

import lombok.Data;

@Data
public class RoleCreateDto {
    private String  roleName;
    private Integer dataScope;
    private String  remark;
    private Integer status;
    private Long    deptId;
    private String authority;
}