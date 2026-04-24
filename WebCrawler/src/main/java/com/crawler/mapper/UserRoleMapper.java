package com.crawler.mapper;

import com.crawler.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    void insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    void deleteByUserId(Long userId);

    void deleteByUserIds(@Param("userIds") List<Long> userIds);

}
