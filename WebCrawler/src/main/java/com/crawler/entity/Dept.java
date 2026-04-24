package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dept {
    private Long deptId;
    private Long parentDeptId;
    private String deptName;
    private Integer deptLevel;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    private List<Dept> children;
}
