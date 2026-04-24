package com.crawler.entity.dto;

import lombok.Data;

@Data
public class RolePageQueryDto {
    private Long roleId;
    private Integer pageNum  = 1;
    private Integer pageSize = 10;
    private String  roleName;
    private Integer status;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}