package com.crawler.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class UserUpdateDto {
    private Integer userId;
    private String password;
    private String phone;
    private Date updateTime;
}
