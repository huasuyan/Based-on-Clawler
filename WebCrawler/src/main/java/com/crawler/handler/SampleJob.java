package com.crawler.handler;

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
        log.info("XXL-Job 测试任务执行成功！");
    }
}