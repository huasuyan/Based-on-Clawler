package com.crawler.controller;

import com.crawler.entity.LoginDto;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("login")
    public Result login(@RequestBody User user) {
        log.info("登录:{}",user);
        LoginDto info = loginService.login(user);

        if(info!=null){
            return Result.success();
        }
        return Result.error("用户名或密码错误");
    }
}
