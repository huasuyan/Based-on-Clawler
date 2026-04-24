package com.crawler.entity.dto.role;

import lombok.Data;

@Data
public class RoleEditDto {
    private Long                roleId;
    private String              roleName;
    private Integer             dataScope;
    private String              remark;
    private Integer             status;
    private String               authority;
}