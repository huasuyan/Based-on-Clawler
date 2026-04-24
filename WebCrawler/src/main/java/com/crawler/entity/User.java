package com.crawler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String username;
    private String password;
    private String phone;
    private Date createTime;

    @JsonIgnore
    private Date updateTime;
    private Long deptId;
    private Integer status;

    private String deptName;
    private String roleNames;

    @JsonIgnore
    private List<Long> roleIds;

    @JsonIgnore
    private String lastOperation;
    private Date lastActiveTime;
}
