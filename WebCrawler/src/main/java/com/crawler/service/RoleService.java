package com.crawler.service;

import com.crawler.entity.User;
import com.crawler.entity.dto.role.RoleCreateDto;
import com.crawler.entity.dto.role.RoleDropdownDto;
import com.crawler.entity.dto.role.RoleEditDto;
import com.crawler.entity.dto.role.RolePageQueryDto;

import java.util.List;
import java.util.Map;

public interface RoleService {
    // 分页列表查询
    Map<String, Object> pageList(RolePageQueryDto queryDto);
    // 新增角色
    Map<String, Object> add(RoleCreateDto createDto);
    // 编辑角色
    Map<String, Object> update(RoleEditDto editDto);
    // 角色详情
    Map<String, Object> detail(Long roleId);
    // 单条删除
    void delete(Long roleId);
    // 批量删除
    void batchDelete(List<Long> roleIds);
    // 查询角色下用户列表
    Map<String, Object> userList(Long roleId, Integer pageNum, Integer pageSize);
    // 角色名称下拉列表
    List<RoleDropdownDto> dropdownList();
    // 查询当前用户所有权限（聚合该用户所有角色的authority）
    Map<String, Object> getAuthority(Long userId);
}