package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test")
public class Controller {
    @Resource
    private XxlJobUtil xxlJobUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @GetMapping("/get")
    public Result getCode(){
        xxlJobUtil.login();
        Object cookie = redisTemplate.opsForValue().get("xxl_job_login_token");
        return Result.success(cookie);
    }


    // SpringBoot 调用你的 Python 接口
    @GetMapping("/test-python")
    public String test() {
        RestTemplate rest = new RestTemplate();
        return rest.getForObject("http://127.0.0.1:8088/", String.class);
    }
}
