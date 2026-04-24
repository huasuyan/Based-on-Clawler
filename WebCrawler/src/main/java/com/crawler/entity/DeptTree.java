package com.crawler.entity;


import lombok.Data;
import java.util.List;

@Data
public class DeptTree {
    private Long deptId;
    private Long parentDeptId;
    private String deptName;
    private Integer deptLevel;
    private Integer status;
    private List<DeptTree> children;
}