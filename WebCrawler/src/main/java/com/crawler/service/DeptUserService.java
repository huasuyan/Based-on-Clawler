package com.crawler.service;

import com.crawler.entity.User;
import com.crawler.entity.dto.UserAddDto;
import com.crawler.entity.dto.UserPageDto;
import com.crawler.entity.dto.UserUpdateDto;

import java.util.List;

public interface DeptUserService {

    void updateUser(UserUpdateDto userUpdateDto);

    User getUserById(Long userId);

    UserPageDto pageList(Integer pageNum, Integer pageSize,Long deptId, String username, Integer status);

    void addUser(UserAddDto dto);

    void deleteUser(Long userId, Long currentUserId);

    void batchDelete(List<Long> userIds, Long currentUserId);

    User detail(Long userId);
}
