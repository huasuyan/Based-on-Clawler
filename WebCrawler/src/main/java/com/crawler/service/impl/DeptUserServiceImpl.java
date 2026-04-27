package com.crawler.service.impl;


import com.crawler.entity.Dept;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserAddDto;
import com.crawler.entity.dto.UserPageDto;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.mapper.DeptMapper;
import com.crawler.mapper.DeptUserMapper;
import com.crawler.mapper.UserRoleMapper;
import com.crawler.service.DeptUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeptUserServiceImpl implements DeptUserService {

    @Autowired
    private DeptUserMapper deptUserMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private DeptMapper deptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDto dto) {
        Long userId = dto.getUserId();

        // 1. 校验用户是否存在
        User existUser = deptUserMapper.selectById(userId);
        if (existUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 2. 校验用户名唯一性（排除当前用户自己）
        if (dto.getUsername() != null && !dto.getUsername().equals(existUser.getUsername())) {
            User sameNameUser = deptUserMapper.selectByUsername(dto.getUsername());
            if (sameNameUser != null) {
                throw new IllegalArgumentException("账号已存在");
            }
        }

        // 3. 组装更新字段
        User user = new User();
        user.setUserId(userId);
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());

        // 4. 更新用户主表
        deptUserMapper.updateUser(user);

        // 先删旧角色 → 再插入新角色（标准多对多更新）
        if (dto.getRoleId() != null) {
            // 先删除旧角色
            userRoleMapper.deleteByUserId(userId);
            // 再插入新角色
            userRoleMapper.insertUserRole(userId, dto.getRoleId());
        }
    }

    @Override
    public User getUserById(Long userId) {
        return deptUserMapper.selectById(userId);
    }

    @Override
    public UserPageDto pageList(Integer pageNum, Integer pageSize,
                                Long deptId, String username, Integer status) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        int offset = (pageNum - 1) * pageSize;

        if (deptId == null) {
            throw new IllegalArgumentException("部门ID不能为空");
        }

        List<User> list = deptUserMapper.selectUserList(deptId, username, status, offset, pageSize);
        Long total = deptUserMapper.countUser(deptId, username, status);

        // 封装返回结果
        UserPageDto vo = new UserPageDto();
        vo.setList(list);
        vo.setTotal(total);
        vo.setPages((total + pageSize - 1) / pageSize);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        return vo;
    }


    private List<Long> getChildDeptIds(Long currentDeptId) {
        List<Long> deptIdList = new ArrayList<>();
        // 递归查询所有子部门ID
        List<Dept> childDepts = deptMapper.selectChildDepts(currentDeptId);
        for (Dept dept : childDepts) {
            deptIdList.add(dept.getDeptId());
            deptIdList.addAll(getChildDeptIds(dept.getDeptId()));
        }
        return deptIdList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserAddDto dto) {
        // 1. 校验用户名唯一
        User existUser = deptUserMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new RuntimeException("账号已存在，请更换账号");
        }

        // 手机号校验
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        // 组装用户对象
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // 插入用户
        deptUserMapper.insertUser(user);
        Long userId = user.getUserId();

        // 多角色插入
        if (dto.getRoleId() != null) {
            userRoleMapper.insertUserRole(userId, dto.getRoleId());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new RuntimeException("不能删除自己");
        }

        // 删除用户
        deptUserMapper.deleteById(userId);
        // 删除角色
        userRoleMapper.deleteByUserId(userId);

    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> userIds, Long currentUserId) {
        for (Long id : userIds) {
            if (id.equals(currentUserId)) {
                throw new RuntimeException("不能删除自己");
            }
        }
        deptUserMapper.batchDelete(userIds);
        userRoleMapper.deleteByUserIds(userIds);

    }

    // 5. 用户详情查看
    public User detail(Long userId) {
        User user = deptUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 密码脱敏
        //
        Long roleId = userRoleMapper.selectRoleIdByUserId(userId);
        user.setRoleId(roleId);
        user.setPassword("********");

        return user;
    }
}

