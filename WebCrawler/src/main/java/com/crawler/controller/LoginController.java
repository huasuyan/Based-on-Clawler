package com.crawler.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import com.crawler.entity.*;
import com.crawler.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
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

    @Value("${jwt.capcha-expiration}")
    private Integer capcha_expiration;
    /**
     * 获取图形验证码
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
        try{
            redisTemplate.opsForValue().set(verify, code, capcha_expiration, TimeUnit.SECONDS);
        }catch (Exception e){
            throw new RuntimeException("网络异常,请重新获取验证码");
        }
        // 返回前端
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);
        map.put("uuid", verify);
        map.put("img", Base64.encode(os.toByteArray()));
        return Result.success(map);
    }

    //用户名密码登录
    @PostMapping("/login")
    public Result login(@RequestBody LoginDto user) {
        String uuid = user.getUuid();
        String code = user.getCode();
        if(!Objects.equals(redisTemplate.opsForValue().get(uuid), code)){
            return Result.error("验证码错误，请重新验证");
        }
        redisTemplate.delete(uuid);
        Map<String,Object> m = loginService.login(user);
        return Result.success(m);
    }

    //验证码登录
    @PostMapping("/codeLogin")
    public Result login(@RequestBody CodeLoginDto codeLoginDto) {
        String uuid = codeLoginDto.getUuid();
        String code = codeLoginDto.getCode();
        String phone = codeLoginDto.getPhone();

        System.out.println(uuid);

        // 1. 先判断 key 是否为空（必须加！）
        if (uuid == null) {
            return Result.error("前端未传入短信验证码uuid"); // 或抛出异常
        }


        // 2. 安全获取
        Object obj = redisTemplate.opsForValue().get(uuid);
        Map<String, Object> map = (Map<String, Object>) obj;


        if(!Objects.equals((String) map.get("code"), code) && !Objects.equals((String) map.get("phone"), phone)){
            return Result.error("验证码错误，请重新输入");
        }
        redisTemplate.delete(uuid);
        Map<String,Object> m = loginService.Codelogin(codeLoginDto);
        return Result.success(m);
    }

    @PostMapping("/sendCode")
    public Result sendCode(@RequestParam String phone) {

        Map<String, Object> map = loginService.generateCode(phone);

        return Result.success(map);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto user) {
        // 1.验证短信验证码
        String uuid = user.getUuid();
        String code = user.getCode();
        redisTemplate.opsForValue().set(uuid, code, 60L, TimeUnit.SECONDS);
        if (!Objects.equals(redisTemplate.opsForValue().get(uuid), code)) {
            return Result.error("验证码错误，请重新验证");
        }
        redisTemplate.delete(uuid);
        // 2.执行注册
        loginService.register(user);
        return Result.success();
    }


    @PostMapping("/delToken")
    public Result delToken(@RequestBody Map<String,Object> map) {
        try {
            // 1.取token
            String token = (String) map.get("token");
            // 2.删token
            redisTemplate.delete(token);
            return Result.success();
        }catch (Exception e){
            return Result.error("退出登录失败，请稍后再试！");
        }

    }


}
