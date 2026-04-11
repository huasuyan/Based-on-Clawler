package com.crawler.service;

import com.crawler.entity.CodeLoginDto;
import com.crawler.entity.LoginDto;
import com.crawler.entity.User;

import java.util.Map;

public interface LoginService {

    Map<String, Object> generateCode(String phone) ;
    Map<String,Object> login(LoginDto user);
    Map<String,Object> Codelogin(CodeLoginDto user);
}
