package com.crawler.handler;

import com.crawler.controller.LoginController;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SampleJob {
    private static final Logger log = LoggerFactory.getLogger(SampleJob.class);

    /**
     * 简单测试任务
     * JobHandler 名称：demoJobHandler
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() {
        LoginController  loginController = new LoginController();
        loginController.getCode();
    }
}