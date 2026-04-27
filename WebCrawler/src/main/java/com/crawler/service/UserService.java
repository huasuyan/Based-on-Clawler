package com.crawler.service;

import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    void updateUser(UserUpdateDto userUpdateDto);

    /**
     * 获取当前用户权限可以访问的用户数据列表
     * @return
     */
    List<Long> getUserList(User user);

    Boolean checkUserVisitDept(User user, Long deptId);

    List<Long> getAllDeptIds(User user);

    User getUserInfo(Long userId);
}
