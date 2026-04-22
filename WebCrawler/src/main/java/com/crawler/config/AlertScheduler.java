package com.crawler.config;

import com.crawler.entity.SpecialAlertSetting;
import com.crawler.mapper.SpecialAlertSettingMapper;
import com.crawler.util.PythonCronAsync;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AlertScheduler {

    @Resource
    private SpecialAlertSettingMapper specialAlertSettingMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void triggerScheduled() {
        List<SpecialAlertSetting> list = specialAlertSettingMapper.selectByTriggerState(1);
        if (list == null || list.isEmpty()) return;

        // 遍历所有启用的专题，如果状态不是运行中，跳过
        for (SpecialAlertSetting specialAlertSetting : list) {
            if (specialAlertSetting.getState() != null && (specialAlertSetting.getState() != 0 && specialAlertSetting.getState() != -1)) {
                log.info("[调度器] alertId={} state={} 跳过",
                        specialAlertSetting.getAlertId(), specialAlertSetting.getState());
                continue;
            }

            if (!isTimeToTrigger(specialAlertSetting)) {
                log.info("[调度器] alertId={} 未到执行间隔，跳过",
                        specialAlertSetting.getAlertId());
                continue;
            }

            log.info("[调度器] 触发 alertId={}", specialAlertSetting.getAlertId());
            pythonCronAsync.callPythonAsync(specialAlertSetting);
        }
    }

    private boolean isTimeToTrigger(SpecialAlertSetting specialAlertSetting) {
        Integer intervalMinutes = specialAlertSetting.getFrequency();
        if (intervalMinutes == null || intervalMinutes <= 0) return true;
        Date lastTime = specialAlertSetting.getLastTriggerTime();
        if (lastTime == null) return true;
        long diffMinutes = (System.currentTimeMillis() - lastTime.getTime()) / 60_000;
        return diffMinutes >= intervalMinutes;
    }
}