package com.crawler.service.impl;

import com.crawler.entity.CodeLoginDto;
import com.crawler.entity.LoginDto;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.mapper.LoginMapper;
import com.crawler.service.LoginService;
import com.crawler.util.JwtUtil;
import com.crawler.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {


    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public Map<String, Object> generateCode(String phone) {

        String code = smsUtil.generateCode(phone);
        String uuid = smsUtil.generateSmsUuid();
        smsUtil.sendSms(phone, code);
        smsUtil.saveSmsCode(phone, code);
        Map<String, Object> map = new HashMap<>();
        map.put("code",code);
        map.put("uuid",uuid);
        return map;
    }

    @Override
    public Map<String,Object> login(LoginDto user) {
        User u = loginMapper.selectByUsernameAndPassword(user);
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
            return m;
        }
    }

}
