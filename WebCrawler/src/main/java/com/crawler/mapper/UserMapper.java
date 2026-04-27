package com.crawler.mapper;

import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper

public interface UserMapper {

    void updateUser(User user);

    User selectById(Long userId);

    String selectUserName(Long userId);

    List<Long> getAllUserId();
}
