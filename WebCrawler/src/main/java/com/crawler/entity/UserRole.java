package com.crawler.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Date createTime;
}