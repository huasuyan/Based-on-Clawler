package com.crawler.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Role {
    private Long roleId;
    private String roleName;
    private Integer dataScope;
    private String remark;
    private Integer status;
    private String authority;   // 数据库存JSON字符串
    private Date createTime;
    private Date updateTime;
}