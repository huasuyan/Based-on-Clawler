package com.crawler.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class UpdateDto {
    private Integer userId;
    private String password;
    private String phone;
    private Date updateTime;
}
