package com.crawler.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDto {
    private Long userId;
    private String username;
    private String password;
    private String phone;
    private Long deptId;
    private Integer status;
    private Long roleId;
}
