package com.crawler.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAddDto {
    private String username;
    private String password;
    private String phone;
    private Long deptId;
    private Integer status;
    private List<Long> roleIds;
}