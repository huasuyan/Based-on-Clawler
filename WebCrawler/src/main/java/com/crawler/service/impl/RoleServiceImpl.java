package com.crawler.service.impl;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.Role;
import com.crawler.entity.dto.role.*;
import com.crawler.mapper.RoleMapper;
import com.crawler.mapper.UserRoleMapper;
import com.crawler.service.PermissionService;
import com.crawler.service.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private PermissionService permissionService;


    /* ------------------------------------------------------------------ */
    /*  分页列表                                                             */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> pageList(RolePageQueryDto queryDto) {
        List<RoleListDto> list = roleMapper.pageList(queryDto)
                .stream()
                .map(RoleListDto::new)
                .collect(Collectors.toList());
        int total = roleMapper.countPageList(queryDto);

        Map<String, Object> result = new HashMap<>();
        result.put("total",    total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("list",     list);
        return result;
    }

    /* ------------------------------------------------------------------ */
    /*  新增角色                                                             */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> add(RoleCreateDto createDto) {
        // 参数校验
        if (createDto.getRoleName() == null || createDto.getRoleName().isBlank()) {
            throw new RuntimeException("角色名称不能为空");
        }
        if (createDto.getDataScope() == null) {
            throw new RuntimeException("数据权限范围不能为空");
        }
        if (createDto.getAuthority() == null || createDto.getAuthority().isEmpty()) {
            throw new RuntimeException("角色权限不能为空");
        }

        // 重名校验
        Role existing = roleMapper.selectByRoleName(createDto.getRoleName());
        if (existing != null) {
            throw new RuntimeException("角色名称已存在");
        }

        Role role = new Role();
        role.setRoleName(createDto.getRoleName());
        role.setDataScope(createDto.getDataScope());
        role.setRemark(createDto.getRemark() != null ? createDto.getRemark() : "");
        role.setStatus(createDto.getStatus() != null ? createDto.getStatus() : 1);
        role.setAuthority(createDto.getAuthority() != null ? createDto.getAuthority() : "");

        roleMapper.insert(role);

        Map<String, Object> result = new HashMap<>();
        result.put("roleId", role.getRoleId());
        return result;
    }

    /* ------------------------------------------------------------------ */
    /*  编辑角色                                                             */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> update(RoleEditDto editDto) {
        if (editDto.getRoleId() == null) {
            throw new RuntimeException("角色ID不能为空");
        }

        // 校验角色是否存在
        Role existing = roleMapper.selectById(editDto.getRoleId());
        if (existing == null) {
            throw new RuntimeException("该角色不存在，修改失败");
        }

        // 重名校验（排除自身）
        if (editDto.getRoleName() != null && !editDto.getRoleName().isBlank()) {
            Role sameNameRole = roleMapper.selectByRoleName(editDto.getRoleName());
            if (sameNameRole != null && !sameNameRole.getRoleId().equals(editDto.getRoleId())) {
                throw new RuntimeException("角色名称已存在，请重新输入");
            }
        }

        roleMapper.update(editDto);

        // 返回更新后的角色信息
        Role updated = roleMapper.selectById(editDto.getRoleId());
        Map<String, Object> result = new HashMap<>();
        result.put("updatedRole", new RoleListDto(updated));

        // 清除该角色下所有用户的权限缓存
        List<Long> affectedUserIds = userRoleMapper.selectUserIdsByRoleId(editDto.getRoleId());
        affectedUserIds.forEach(permissionService::evictCache);

        return result;
    }

    /* ------------------------------------------------------------------ */
    /*  角色详情                                                             */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> detail(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("该角色不存在，无法查看详情");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", new RoleListDto(role));
        return result;
    }

    /* ------------------------------------------------------------------ */
    /*  单条删除                                                             */
    /* ------------------------------------------------------------------ */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long roleId) {
        Role existing = roleMapper.selectById(roleId);
        if (existing == null) {
            throw new RuntimeException("该角色不存在，无法删除");
        }

        // 校验是否有用户关联
        int userCount = roleMapper.countUsersByRoleId(roleId);
        if (userCount > 0) {
            throw new RuntimeException("该角色下存在关联用户，无法删除");
        }

        if (existing.getStatus() ==1) {
            throw new RuntimeException("该角色为启用状态，无法删除");
        }

        roleMapper.deleteById(roleId);
        userRoleMapper.deleteByRoleId(roleId);
    }

    /* ------------------------------------------------------------------ */
    /*  批量删除                                                             */
    /* ------------------------------------------------------------------ */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDelete(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("请选择要删除的角色");
        }

        // 校验是否有用户关联
        int userCount = userRoleMapper.countByRoleIds(roleIds);
        if (userCount > 0) {
            throw new RuntimeException("选中的角色中存在关联用户，无法删除");
        }

        roleMapper.batchDelete(roleIds);
        userRoleMapper.batchDeleteByRoleIds(roleIds);
    }

    /* ------------------------------------------------------------------ */
    /*  角色下用户列表                                                        */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> userList(Long roleId, Integer pageNum, Integer pageSize) {
        // 校验角色是否存在
        Role existing = roleMapper.selectById(roleId);
        if (existing == null) {
            throw new RuntimeException("角色不存在");
        }

        int offset = (pageNum - 1) * pageSize;
        List<RoleUserListDto> list = roleMapper.selectUsersByRoleId(roleId, offset, pageSize);
        int total = roleMapper.countUsersByRoleId(roleId);
        int pages = (total + pageSize - 1) / pageSize;

        Map<String, Object> result = new HashMap<>();
        result.put("total",    total);
        result.put("pages",    pages);
        result.put("pageNum",  pageNum);
        result.put("pageSize", pageSize);
        result.put("list",     list);
        return result;
    }

    /* ------------------------------------------------------------------ */
    /*  角色下拉列表                                                          */
    /* ------------------------------------------------------------------ */
    @Override
    public List<RoleDropdownDto> dropdownList() {
        return roleMapper.selectAllEnabled()
                .stream()
                .map(RoleDropdownDto::new)
                .collect(Collectors.toList());
    }
    /* ------------------------------------------------------------------ */
    /*  当前用户所有权限（聚合所有角色authority，同名字段取最大值 1优先）           */
    /* ------------------------------------------------------------------ */
    @Override
    public Map<String, Object> getAuthority(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> mergedAuthority = new HashMap<>();
        for (Long roleId : roleIds) {
            Role role = roleMapper.selectById(roleId);
            if (role == null || role.getAuthority() == null) continue;
            if (role.getStatus() != null && role.getStatus() == 0) continue; // 跳过禁用角色

            Map<String, Object> authorityMap = JSONUtil.parseObj(role.getAuthority());
            for (Map.Entry<String, Object> entry : authorityMap.entrySet()) {
                String key   = entry.getKey();
                Object value = entry.getValue();
                // 取最大值：有1就是1
                Object existing2 = mergedAuthority.get(key);
                if (existing2 == null) {
                    mergedAuthority.put(key, value);
                } else {
                    int cur  = Integer.parseInt(value.toString());
                    int prev = Integer.parseInt(existing2.toString());
                    mergedAuthority.put(key, Math.max(cur, prev));
                }
            }
        }
        return mergedAuthority;
    }

}