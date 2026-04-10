package com.crawler.service;

import com.crawler.entity.LoginDto;
import com.crawler.entity.User;

import java.util.Map;

public interface LoginService {

    Map<String,Object> login(LoginDto user);
}
