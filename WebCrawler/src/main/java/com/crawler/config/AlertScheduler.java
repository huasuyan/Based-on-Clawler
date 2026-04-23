package com.crawler.config;

import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.SpecialReportSetting;
import com.crawler.mapper.SpecialAlertSettingMapper;
import com.crawler.mapper.SpecialReportSettingMapper;
import com.crawler.util.PythonCronAsync;
import com.crawler.util.ReportAsync;
import com.crawler.util.ReportScheduleUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AlertScheduler {

    @Resource
    private SpecialAlertSettingMapper specialAlertSettingMapper;

    @Resource
    private SpecialReportSettingMapper specialReportSettingMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    @Resource
    private ReportAsync reportAsync;

    @Resource
    private ReportScheduleUtil reportScheduleUtil;

    /**
     * 每分钟扫描一次：
     * 1. 预警专题调度（原有逻辑，不动）
     * 2. 报告专题调度（新增）
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void triggerScheduled() {
        triggerAlerts();
        triggerReports();
    }

    // ----------------------------------------------------------------
    //  原有预警专题调度逻辑，保持不变
    // ----------------------------------------------------------------
    private void triggerAlerts() {
        List<SpecialAlertSetting> list =
                specialAlertSettingMapper.selectByTriggerState(1);
        if (list == null || list.isEmpty()) return;

        for (SpecialAlertSetting specialAlertSetting : list) {
            if (specialAlertSetting.getState() != null
                    && (specialAlertSetting.getState() != 0
                    && specialAlertSetting.getState() != -1)) {
                log.info("[调度器-预警] alertId={} state={} 跳过",
                        specialAlertSetting.getAlertId(), specialAlertSetting.getState());
                continue;
            }
            if (!isTimeToTriggerAlert(specialAlertSetting)) {
                log.info("[调度器-预警] alertId={} 未到执行间隔，跳过",
                        specialAlertSetting.getAlertId());
                continue;
            }
            log.info("[调度器-预警] 触发 alertId={}", specialAlertSetting.getAlertId());
            pythonCronAsync.callPythonAsync(specialAlertSetting);
        }
    }

    private boolean isTimeToTriggerAlert(SpecialAlertSetting specialAlertSetting) {
        Integer intervalMinutes = specialAlertSetting.getFrequency();
        if (intervalMinutes == null || intervalMinutes <= 0) return true;
        Date lastTime = specialAlertSetting.getLastTriggerTime();
        if (lastTime == null) return true;
        long diffMinutes =
                (System.currentTimeMillis() - lastTime.getTime()) / 60_000;
        return diffMinutes >= intervalMinutes;
    }

    // ----------------------------------------------------------------
    //  报告专题调度逻辑
    // ----------------------------------------------------------------
    private void triggerReports() {
        List<SpecialReportSetting> list =
                specialReportSettingMapper.selectAllEnabled();
        if (list == null || list.isEmpty()) return;

        for (SpecialReportSetting setting : list) {
            // 正在执行中则跳过（executeStatus: 1=爬取中 2=生成报告中）
            if (setting.getExecuteStatus() != null
                    && (setting.getExecuteStatus() == 1
                    || setting.getExecuteStatus() == 2)) {
                log.info("[调度器-报告] specialReportId={} executeStatus={} 正在执行，跳过",
                        setting.getSpecialReportId(), setting.getExecuteStatus());
                continue;
            }

            if (isTimeToTriggerReport(setting)){
                log.info("[调度器-报告] 触发爬虫更新 specialReportId={}", setting.getSpecialReportId());
                reportAsync.updateDataAsync(setting);
            }

            if (reportScheduleUtil.shouldTrigger(setting)) {
                log.info("[调度器-报告] 触发生成报告 specialReportId={}", setting.getSpecialReportId());
                reportAsync.runReportAsync(setting);
            }
        }
    }

    private boolean isTimeToTriggerReport(SpecialReportSetting specialReportSetting) {
        Date updateTime = specialReportSetting.getLastUpdateTime();
        if (updateTime == null) return true;
        Instant update = updateTime.toInstant();
        Instant now = Instant.now();
        Duration duration = Duration.between(update, now);
        return duration.toHours() >= 24;
    }
}
