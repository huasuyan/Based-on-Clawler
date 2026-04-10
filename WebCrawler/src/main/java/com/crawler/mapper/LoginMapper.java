package com.crawler.mapper;

import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {
    User selectByUsernameAndPassword(User user);
}
