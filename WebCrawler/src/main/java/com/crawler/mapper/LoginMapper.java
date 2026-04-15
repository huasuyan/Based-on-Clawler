package com.crawler.mapper;

import com.crawler.entity.dto.LoginDto;
import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    User selectByUsernameAndPassword(LoginDto user);

    User selectByUsername(String user);
    User selectByPhone(String user);

    void insertUser(User user);
}
