package com.crawler.service;

import com.crawler.entity.Dept;
import com.crawler.entity.Result;

import java.util.List;
public interface DeptService {
    Result getDeptTree(Long deptId, Integer showEnable);

    void addTopDept(Dept dept);

    void addChildDept(Dept dept);

    void updateDept(Dept dept);

    void deleteDept(Long deptId);

    Result searchDeptTree(Long deptId, String searchName, Integer showEnable);
}
