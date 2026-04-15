package com.crawler.controller;


import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.UpdateDto;
import com.crawler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //从请求中获取当前登录用户
    @PostMapping("/update")
    public Result updateUser(HttpServletRequest request, UpdateDto updateDto) {
//         从请求中获取当前登录用户
        User currentUser = (User) request.getAttribute("currentUser");
        updateDto.setUserId(currentUser.getUserId());

        updateDto.setUpdateTime(new Date());

        try {
            userService.updateUser(updateDto);
            return Result.success("用户信息修改成功");
        } catch (Exception e) {
            return Result.error("用户信息修改失败：" + e.getMessage());
        }
    }
}
