package com.crawler.service;

import com.crawler.entity.dto.CodeLoginDto;
import com.crawler.entity.dto.LoginDto;
import com.crawler.entity.dto.RegisterDto;

import java.util.Map;

public interface LoginService {

    Map<String, Object> generateCode(String phone) ;
    Map<String,Object> login(LoginDto user);
    Map<String,Object> Codelogin(CodeLoginDto user);

    void register(RegisterDto user);
}
