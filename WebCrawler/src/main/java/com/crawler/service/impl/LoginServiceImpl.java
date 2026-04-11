package com.crawler.service.impl;

import com.crawler.entity.CodeLoginDto;
import com.crawler.entity.LoginDto;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.mapper.LoginMapper;
import com.crawler.service.LoginService;
import com.crawler.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.token-expiration}")
    private Integer token_expiration;
    @Autowired
    private LoginMapper loginMapper;
    @Override
    public Map<String,Object> login(LoginDto user) {
        User u = loginMapper.selectByUsernameAndPassword(user);
        System.out.println(u);
        if(u!=null){
            return getToken(u);
        }
        throw new RuntimeException("用户名或密码错误");
    }

    @Override
    public Map<String,Object> Codelogin(CodeLoginDto user) {
        User u = loginMapper.selectByPhone(user);
        if(u!=null){
            return getToken(u);
        }
        throw new RuntimeException("该手机号未注册");
    }

    private Map<String, Object> getToken(User u) {
        {
            Map<String,Object> claims = new HashMap<>();
            claims.put("userId",u.getUserId());
            claims.put("username",u.getUsername());
            String jwt = JwtUtil.generateToken(String.valueOf(u.getUserId()),claims);
            Map<String,Object> m =new HashMap<>();
            m.put("token",jwt);
            redisTemplate.opsForValue().set(jwt, u, token_expiration, TimeUnit.SECONDS);
            return m;
        }
    }
}
