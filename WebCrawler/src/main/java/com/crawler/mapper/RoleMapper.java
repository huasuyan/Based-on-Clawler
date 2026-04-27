package com.crawler.mapper;

import com.crawler.entity.Role;
import com.crawler.entity.dto.role.RoleEditDto;
import com.crawler.entity.dto.role.RolePageQueryDto;
import com.crawler.entity.dto.role.RoleUserListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    // 新增角色
    void insert(Role role);
    // 根据ID查询
    Role selectById(Long roleId);
    // 根据角色名查询（重名校验用）
    Role selectByRoleName(@Param("roleName") String roleName);
    // 分页列表查询
    List<Role> pageList(RolePageQueryDto queryDto);
    // 统计总条数（分页用）
    int countPageList(RolePageQueryDto queryDto);
    // 编辑角色
    void update(RoleEditDto editDto);
    // 删除角色
    void deleteById(Long roleId);
    // 批量删除角色
    void batchDelete(@Param("roleIds") List<Long> roleIds);
    // 查询启用状态角色（下拉列表用）
    List<Role> selectAllEnabled(@Param("deptIdList") List<Long> deptIdList);
    // 查询某角色下的用户列表（关联user、dept表）
    List<RoleUserListDto> selectUsersByRoleId(@Param("roleId")   Long roleId,
                                              @Param("offset")   int  offset,
                                              @Param("pageSize") int  pageSize);
    // 统计某角色下用户总数
    int countUsersByRoleId(@Param("roleId") Long roleId);
}