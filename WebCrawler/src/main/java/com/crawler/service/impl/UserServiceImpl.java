package com.crawler.service.impl;


import com.crawler.entity.dto.UpdateDto;
import com.crawler.mapper.UserMapper;
import com.crawler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public void updateUser(UpdateDto updateDto) {

        userMapper.updateUser(updateDto);
    }
}
