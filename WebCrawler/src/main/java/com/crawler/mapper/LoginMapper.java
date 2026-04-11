package com.crawler.mapper;

import com.crawler.entity.CodeLoginDto;
import com.crawler.entity.LoginDto;
import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {
    User selectByUsernameAndPassword(LoginDto user);
    User selectByPhone(CodeLoginDto user);
}
