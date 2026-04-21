package com.crawler.config;

import com.crawler.entity.CrawlerCron;
import com.crawler.mapper.CrawlerCronMapper;
import com.crawler.util.PythonCronAsync;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 预警专题定时调度器
 *
 * 扫描所有 trigger_state=1（已启用）的专题，
 * 根据 frequency 决定是否触发本轮爬取：
 *   0：实时（每分钟触发）
 *   1：定时（每小时触发，可按需调整）
 *   2：定量（由 alert_trigger 控制，暂按每小时触发）
 *
 * 注意：@Scheduled 固定频率扫描，业务频率由 frequency 字段控制。
 */
@Slf4j
@Component
public class CrawlerCronScheduler {

    @Resource
    private CrawlerCronMapper crawlerCronMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    /**
     * 定时模式：每十分钟执行一次
     * 可根据实际需求修改 cron 表达式
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void triggerScheduled() {
        triggerByFrequency();
    }

    private void triggerByFrequency() {
        // 查询所有已启用的专题
        List<CrawlerCron> list = crawlerCronMapper.selectByTriggerState(1);

        if (list == null || list.isEmpty()) {
            return;
        }

        for (CrawlerCron crawlerCron : list) {
            // 只有 state=0（等待中）才触发，避免上次还没跑完又重复触发
            if (crawlerCron.getState() != null && crawlerCron.getState() == 0) {
                log.info("[调度器] 触发专题 crawlerId={}", crawlerCron.getCrawlerId());
                pythonCronAsync.callPythonAsync(crawlerCron);
            } else {
                log.info("[调度器] 专题 crawlerId={} 当前 state={}，跳过本轮",
                        crawlerCron.getCrawlerId(), crawlerCron.getState());
            }
        }
    }
}