package com.crawler.mapper;

import com.crawler.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeptUserMapper {

    List<User> selectUserList(
            @Param("deptId") Long deptId,
            @Param("username") String username,
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    long countUser(
            @Param("deptId") Long deptId,
            @Param("username") String username,
            @Param("status") Integer status
    );

    User selectById(Long userId);

    User selectByUsername(String username);

    int insertUser(User user);
    int updateUser(User user);
    int deleteById(Long userId);
    int batchDelete(@Param("userIds") List<Long> userIds);
}


