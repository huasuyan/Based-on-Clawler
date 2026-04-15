package com.crawler.entity.dto;

import com.crawler.entity.User;
import lombok.Data;

@Data
public class UserInfoDto {
    private Integer userId;
    private String userName;
    private String phone;

//    这个Dto是留给查询个人信息的
    public UserInfoDto(User u) {
        this.userId = u.getUserId();
        this.userName = u.getUsername();
        this.phone = u.getPhone();
    }
}
