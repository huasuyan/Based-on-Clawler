package com.crawler.service;

import com.crawler.entity.LoginDto;
import com.crawler.entity.User;

public interface LoginService {

    LoginDto login(User user);
}
