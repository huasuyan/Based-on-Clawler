package com.crawler.mapper;

import com.crawler.entity.UpdateDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void updateUser(UpdateDto updateDto);

}

