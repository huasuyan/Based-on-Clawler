package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.UserUpdateDto;
import com.crawler.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("update")
    public Result updateUser(HttpServletRequest request, @RequestBody UserUpdateDto userUpdateDto) {

        User currentUser = (User) request.getAttribute("currentUser");
        userUpdateDto.setUserId(currentUser.getUserId());

        userService.updateUser(userUpdateDto);
        return Result.success("修改成功");
    }


    @PostMapping("info")
    public Result info(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        User user = userService.getUserInfo(currentUser.getUserId());
        return Result.success(user);
    }
}
