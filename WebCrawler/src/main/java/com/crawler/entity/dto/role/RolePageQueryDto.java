package com.crawler.entity.dto.role;

import lombok.Data;

import java.util.List;

@Data
public class RolePageQueryDto {
    private Long roleId;
    private List<Long> deptIdList;
    private Integer pageNum  = 1;
    private Integer pageSize = 10;
    private String  roleName;
    private Integer status;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}