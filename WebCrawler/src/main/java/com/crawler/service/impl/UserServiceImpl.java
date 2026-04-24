package com.crawler.service.impl;


import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.mapper.UserMapper;
import com.crawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public void updateUser(UserUpdateDto userUpdateDto) {

        userMapper.updateUser(userUpdateDto);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
