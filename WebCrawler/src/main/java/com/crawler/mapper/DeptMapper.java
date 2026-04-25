package com.crawler.mapper;

import com.crawler.entity.Dept;
import com.crawler.entity.DeptTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeptMapper{

    List<Dept> selectAll();

    Dept selectByParentAndName(@Param("parentDeptId") Long parentDeptId, @Param("deptName") String deptName);

    void insert(Dept dept);

    Dept selectById(Long parentId);

    void update(Dept dept);

    int countChildrenDept(@Param("deptId") Long deptId);

    //查询 user 表中是否存在该部门的用户
    int countUserByDeptId(@Param("deptId") Long deptId);

    //模糊查询部门（用于搜索）
    List<Dept> selectByDeptNameLike(@Param("searchName") String searchName);

    //查询部门的所有上级部门ID（用于搜索时补全路径）
    List<Long> selectParentIdsByDeptId(@Param("deptId") Long deptId);

    //删除部门
    int deleteById(@Param("deptId") Long deptId);

    List<Dept> selectAllActive();
}
