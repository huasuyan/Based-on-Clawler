package com.crawler.service.impl;

import com.crawler.entity.*;
import com.crawler.mapper.LoginMapper;
import com.crawler.service.LoginService;
import com.crawler.util.JwtUtil;
import com.crawler.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    @Autowired
    private SmsUtil smsUtil;

    //生成电话注册验证码
    @Override
    public Map<String, Object> generateCode(String phone) {

        String code = smsUtil.generateCode();
        String uuid = smsUtil.generateSmsUuid();
        smsUtil.sendSms(phone, code);
        smsUtil.saveSmsCode(phone, code, uuid);
        Map<String, Object> map = new HashMap<>();
        map.put("uuid",uuid);
        return map;
    }

    //用户名密码登录
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
        User u = loginMapper.selectByPhone(user.getPhone());
        if(u!=null){
            return getToken(u);
        }
        throw new RuntimeException("该手机号未注册");
    }

    @Override
    public void register(RegisterDto dto) {
        // 1. 检查用户名是否已存在
        if (loginMapper.selectByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 2. 检查手机号是否已注册
        if (loginMapper.selectByPhone(dto.getPhone()) != null) {
            throw new RuntimeException("该手机号已注册");
        }
        // 3. 构建用户对象并插入
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // 生产环境建议加密，见注意事项
        user.setPhone(dto.getPhone());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        loginMapper.insertUser(user);
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
