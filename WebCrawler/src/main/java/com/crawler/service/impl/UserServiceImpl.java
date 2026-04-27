package com.crawler.service.impl;

import com.crawler.entity.Dept;
import com.crawler.entity.Role;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.mapper.DeptMapper;
import com.crawler.mapper.RoleMapper;
import com.crawler.mapper.UserMapper;
import com.crawler.mapper.UserRoleMapper;
import com.crawler.service.DeptService;
import com.crawler.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public void updateUser(UserUpdateDto userUpdateDto) {
        // 更新用户信息
        User user = new User();
        user.setUserId(userUpdateDto.getUserId());
        user.setPassword(userUpdateDto.getPassword());
        user.setPhone(userUpdateDto.getPhone());
        user.setUpdateTime(new Date());
        userMapper.updateUser(user);
    }

    @Override
    public List<Long> getUserList(User user) {
        user.setRoleId(userRoleMapper.selectRoleIdByUserId(user.getUserId()));
        // 获取当前用户的角色信息
        Role roleInfo = roleMapper.selectById(user.getRoleId());

        // 根据用户权限获取可访问的部门ID
        Integer dataScope = roleInfo.getDataScope();
        List<Long> allowDepts = new ArrayList<>();
        if(dataScope==1){
            // 获取用户下级部门
            allowDepts = getAllSubDept(user.getDeptId());
            allowDepts.add(user.getDeptId());
            // 根据allowDepts去查询所有用户ID
            return deptMapper.listUserIdByDeptIdList(allowDepts);

        }else if(dataScope==2){
            // 获取到用户的单位ID（父部门Id为0）
            Long topDeptId = deptMapper.getTopLevelParentId(user.getDeptId());
            allowDepts = getAllSubDept(topDeptId);
            allowDepts.add(topDeptId);
            return deptMapper.listUserIdByDeptIdList(allowDepts);
        }else if(dataScope==3){
            return userMapper.getAllUserId();
        }

        return List.of();


    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        user.setPassword("*************");
        return user;
    }

    /**
     * 获取指定部门的所有下级（含所有层级子孙）
     * @param rootDeptId 传入的起始部门ID
     * @return 所有下级部门列表
     */
    public List<Long> getAllSubDept(Long rootDeptId) {
        List<Long> result = new ArrayList<>();
        List<Dept> allDeptList =  deptMapper.selectAll();
        recursionFindChildren(rootDeptId, allDeptList, result);
        return result;
    }

    /**
     * 递归查找子部门
     */
    private void recursionFindChildren(Long parentId,List<Dept> allDeptList, List<Long> result) {
        for (Dept dept : allDeptList) {
            // 找到当前parent的直接下级
            if (Objects.equals(dept.getParentDeptId(), parentId)) {
                result.add(dept.getDeptId());
                // 继续递归找这个部门的下级
                recursionFindChildren(dept.getDeptId(), allDeptList, result);
            }
        }
    }

}
