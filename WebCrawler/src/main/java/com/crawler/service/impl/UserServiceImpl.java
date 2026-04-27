package com.crawler.service.impl;

import com.crawler.entity.Dept;
import com.crawler.entity.Role;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.mapper.DeptMapper;
import com.crawler.mapper.RoleMapper;
import com.crawler.mapper.UserMapper;
import com.crawler.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;

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
    public List<User> getUserList(User user) {
        // 获取当前用户的角色信息
        Role roleInfo = roleMapper.selectById(user.getRoleId());

        // 根据用户权限获取可访问的部门ID
        Integer dataScope = roleInfo.getDataScope();
        List<Long> allowDepts = new ArrayList<>();
        if(dataScope==1){
            allowDepts.add(user.getUserId());
            List<Dept> allDeptList = deptMapper.selectAll();
            // 获取用户下级部门

        }else if(dataScope==2){
            allowDepts.add(user.getUserId());
        }else if(dataScope==3){

        }

        return List.of();


    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        user.setPassword("*************");
        return user;
    }

}
