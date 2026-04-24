package com.crawler.mapper;

import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;
@Mapper

public interface UserMapper {

    void updateUser(User user);

    User selectById(Long userId);
}
