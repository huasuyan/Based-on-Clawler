package com.crawler.mapper;

import com.crawler.entity.dto.UserUpdateDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void updateUser(UserUpdateDto userUpdateDto);

}

