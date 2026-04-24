package com.crawler.service;

import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;

public interface UserService {
    void updateUser(UserUpdateDto userUpdateDto);


    User getUserInfo(Long userId);
}
