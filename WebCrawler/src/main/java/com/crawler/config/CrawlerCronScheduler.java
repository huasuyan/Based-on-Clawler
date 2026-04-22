package com.crawler.config;

import com.crawler.entity.CrawlerCron;
import com.crawler.mapper.CrawlerCronMapper;
import com.crawler.util.PythonCronAsync;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class CrawlerCronScheduler {

    @Resource
    private CrawlerCronMapper crawlerCronMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void triggerScheduled() {
        List<CrawlerCron> list = crawlerCronMapper.selectByTriggerState(1);
        if (list == null || list.isEmpty()) return;

        // 遍历所有启用的专题，如果状态不是运行中，跳过
        for (CrawlerCron crawlerCron : list) {
            if (crawlerCron.getState() != null && (crawlerCron.getState() != 0 && crawlerCron.getState() != -1)) {
                log.info("[调度器] crawlerId={} state={} 跳过",
                        crawlerCron.getCrawlerId(), crawlerCron.getState());
                continue;
            }

            if (!isTimeToTrigger(crawlerCron)) {
                log.info("[调度器] crawlerId={} 未到执行间隔，跳过",
                        crawlerCron.getCrawlerId());
                continue;
            }

            log.info("[调度器] 触发 crawlerId={}", crawlerCron.getCrawlerId());
            pythonCronAsync.callPythonAsync(crawlerCron);
        }
    }

    private boolean isTimeToTrigger(CrawlerCron crawlerCron) {
        Integer intervalMinutes = crawlerCron.getFrequency();
        if (intervalMinutes == null || intervalMinutes <= 0) return true;
        Date lastTime = crawlerCron.getLastTriggerTime();
        if (lastTime == null) return true;
        long diffMinutes = (System.currentTimeMillis() - lastTime.getTime()) / 60_000;
        return diffMinutes >= intervalMinutes;
    }
}