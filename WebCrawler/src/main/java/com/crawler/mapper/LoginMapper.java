package com.crawler.mapper;

import com.crawler.entity.LoginDto;
import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface LoginMapper {
    User selectByUsernameAndPassword(LoginDto user);
}
