package com.crawler.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialReportSetting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 报告调度判断工具
 * 判断当前时刻是否应触发某个报告专题的执行
 */
@Slf4j
@Component
public class ReportScheduleUtil {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 判断当前时刻是否需要触发该专题的报告生成
     *
     * @param setting 报告专题配置
     * @return true=需要触发
     */
    public boolean shouldTrigger(SpecialReportSetting setting) {
        if (setting.getTypeParams() == null
                || StringUtils.isBlank(setting.getTypeParams().toString())) {
            log.warn("[调度判断] typeParams为空，specialReportId={}", setting.getSpecialReportId());
            return false;
        }

        try {
            JSONObject tp = JSONUtil.parseObj(setting.getTypeParams().toString());
            Integer reportType = setting.getReportType();

            if (reportType == 1) {
                return shouldTriggerInstant(setting, tp);
            } else if (reportType == 2) {
                return shouldTriggerScheduled(setting, tp);
            }
        } catch (Exception e) {
            log.error("[调度判断] 解析typeParams失败，specialReportId={}",
                    setting.getSpecialReportId(), e);
        }
        return false;
    }

    /**
     * 即时报告触发逻辑：
     * - 当前时间在end_date之后
     */
    private boolean shouldTriggerInstant(SpecialReportSetting setting, JSONObject tp) {
        String startDateStr = tp.getStr("start_date");
        String endDateStr   = tp.getStr("end_date");
        if (startDateStr == null || endDateStr == null) return false;

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate   = LocalDate.parse(endDateStr);
        LocalDate today     = LocalDate.now();

        // 不在时间窗口内
        if (today.isBefore(startDate)) return false;

        // 未到截止时间
        if(!today.isAfter(endDate)) return false;
        return true;
    }

    /**
     * 定时报告触发逻辑：
     * - 判断 cycle（daily/weekly/monthly）
     * - 当前时间（精确到分钟）与配置的时间一致
     * - 今天此时间点还未执行过
     */
    private boolean shouldTriggerScheduled(SpecialReportSetting setting, JSONObject tp) {
        String cycle      = tp.getStr("cycle");
        String configTime = tp.getStr("time");   // 例如 "08:00"
        if (cycle == null || configTime == null) return false;

        LocalDateTime now        = LocalDateTime.now();
        LocalTime     nowTime    = now.toLocalTime().withSecond(0).withNano(0);
        LocalTime     targetTime = LocalTime.parse(configTime, TIME_FMT);

        // 当前分钟是否与配置时间一致
        if (!nowTime.equals(targetTime)) return false;

        // 检查是否今天此分钟已执行过（防止一分钟内多次触发）
        if (setting.getLastExecuteTime() != null) {
            LocalDateTime lastDt = setting.getLastExecuteTime()
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withSecond(0).withNano(0);
            if (lastDt.equals(now.withSecond(0).withNano(0))) return false;
        }

        switch (cycle) {
            case "daily":
                return true;

            case "weekly": {
                Integer weekday = tp.getInt("weekday");  // 1=周一 ... 7=周日
                if (weekday == null) return false;
                // Java DayOfWeek: MONDAY=1 ... SUNDAY=7
                return now.getDayOfWeek().getValue() == weekday;
            }

            case "monthly": {
                Integer day = tp.getInt("day");          // 每月几号
                if (day == null) return false;
                return now.getDayOfMonth() == day;
            }

            default:
                log.warn("[调度判断] 未知cycle={}，specialReportId={}",
                        cycle, setting.getSpecialReportId());
                return false;
        }
    }
}
