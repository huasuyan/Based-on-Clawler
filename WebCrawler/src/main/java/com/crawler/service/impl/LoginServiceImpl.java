package com.crawler.service.impl;

import com.crawler.entity.LoginDto;
import com.crawler.entity.User;
import com.crawler.mapper.LoginMapper;
import com.crawler.service.LoginService;
import com.crawler.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {


    @Autowired
    private LoginMapper loginMapper;
    @Override
    public LoginDto login(User user) {

        User u = loginMapper.selectByUsernameAndPassword(user);

        if(u!=null){
            Map<String,Object> claims = new HashMap<>();
            claims.put("userId",u.getUserId());
            claims.put("username",u.getUsername());

            String jwt = JwtUtil.generateToken(String.valueOf(u.getUserId()),claims);

            return new LoginDto(u.getUserId(),u.getUsername(),jwt);
        }
        return null;
    }

}
