package com.crawler.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import com.crawler.entity.LoginDto;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class LoginController {
    //注入RedisTemplate
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LoginService loginService;

    /**
     * 获取验证码
     */
    @GetMapping("/getCode")
    public Result getCode() {
        // 生成验证码图片
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        String verify = IdUtil.simpleUUID();
        String code = lineCaptcha.getCode();
        // 输出图片流
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        lineCaptcha.write(os);
        //存入Redis，有效期1分钟
        redisTemplate.opsForValue().set("captcha:" + verify, code, 60L, TimeUnit.SECONDS);
        // 返回前端
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);
        map.put("uuid", verify);
        map.put("img", Base64.encode(os.toByteArray()));
        return Result.success(map);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDto user) {

        Map<String,Object> m = loginService.login(user);
        return Result.success(m);
    }

}
