package com.crawler.config;

import com.crawler.service.DashboardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DashboardScheduler {

    @Resource
    private DashboardService dashboardService;

    /**
     * 每小时整点刷新一次大屏统计数据
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledRefresh() {
        log.info("[Dashboard定时任务] 触发大屏数据刷新");
        dashboardService.refreshStats();
    }
}