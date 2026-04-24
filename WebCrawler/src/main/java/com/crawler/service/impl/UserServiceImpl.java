package com.crawler.service.impl;

import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.mapper.UserMapper;
import com.crawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        user.setPassword("*************");
        return user;
    }

}
