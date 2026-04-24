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

    // 查询角色下是否有关联用户（删除前校验）
    int countByRoleId(@Param("roleId") Long roleId);
    // 批量校验多个角色是否有关联用户
    int countByRoleIds(@Param("roleIds") List<Long> roleIds);
    // 删除角色关联数据（单条）
    void deleteByRoleId(@Param("roleId") Long roleId);
    // 批量删除角色关联数据
    void batchDeleteByRoleIds(@Param("roleIds") List<Long> roleIds);
    // 查询用户绑定的所有角色
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    // 查询角色下所有用户
    List<Long> selectUserIdsByRoleId(Long roleId);
}