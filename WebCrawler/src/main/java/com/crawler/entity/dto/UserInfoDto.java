package com.crawler.entity.dto;

import com.crawler.entity.User;
import lombok.Data;

@Data
public class UserInfoDto {
    private Long userId;
    private String username;
    private String phone;

//    这个Dto是留给查询个人信息的
    public UserInfoDto(User u) {
        this.userId = u.getUserId();
        this.username = u.getUsername();
        this.phone = u.getPhone();
    }
}
