package com.crawler.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.service.AlertMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AlertUtil {

    @Resource
    private AlertMessageService alertMessageService;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(4);

    /**
     * 异步发送预警消息
     * 若当前不在 time_range 内，则延迟到允许时间点再发
     */
    @Async
    public void sendAlertAsync(SpecialAlertSetting specialAlertSetting, int newCount) {
        long delayMs = calcDelayMs(specialAlertSetting.getTimeRange());
        if (delayMs <= 0) {
            doSendAlert(specialAlertSetting, newCount);
        } else {
            log.info("[预警] alertId={} 当前不在预警时间内，延迟 {}ms 后发送",
                    specialAlertSetting.getAlertId(), delayMs);
            scheduler.schedule(
                    () -> doSendAlert(specialAlertSetting, newCount),
                    delayMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * 实际发送逻辑
     */
    private void doSendAlert(SpecialAlertSetting specialAlertSetting, int newCount) {
        Integer alertMethod = specialAlertSetting.getAlertMethod();
        String alertName = specialAlertSetting.getAlertName();

        String content = String.format(
                "【快爬预警】专题「%s」检测到 %d 条新舆情，请及时查看。",
                alertName, newCount
        );

        log.info("[预警] alertId={} alertMethod={} 发送：{}",
                specialAlertSetting.getAlertId(), alertMethod, content);

            sendInternalMessage(specialAlertSetting, content);
    }

    /**
     * 站内信：持久化 + WebSocket 推送
     */
    private void sendInternalMessage(SpecialAlertSetting specialAlertSetting, String content) {
        try {
            alertMessageService.saveAndPush(
                    specialAlertSetting.getUserId(),
                    specialAlertSetting.getAlertId(),
                    specialAlertSetting.getAlertName(),
                    content
            );
            log.info("[预警-站内信] 已持久化并推送 alertId={}", specialAlertSetting.getAlertId());
        } catch (Exception e) {
            log.error("[预警-站内信] 推送失败", e);
        }
    }

    // ----------------------------------------------------------------
    //  time_range 解析与延迟计算
    // ----------------------------------------------------------------

    /**
     * 计算距离下一个允许发送时间点的毫秒数
     * 返回 0 表示当前即可发送
     */
    public long calcDelayMs(String timeRangeJson) {
        if (timeRangeJson == null || timeRangeJson.isBlank()) {
            return 0; // null 表示任何时间都可以
        }

        try {
            JSONObject tr = JSONUtil.parseObj(timeRangeJson);
            // weekdays: [0,1,1,1,1,1,0] 索引0=周日，1=周一...6=周六
            // 与 Java DayOfWeek: MON=1...SUN=7 注意映射
            int[] weekdays = tr.getJSONArray("weekdays")
                    .toList(Integer.class)
                    .stream()
                    .mapToInt(Integer::intValue)
                    .toArray();

            JSONObject time = tr.getJSONObject("time");
            LocalTime startTime = LocalTime.parse(time.getStr("start"));
            LocalTime endTime   = LocalTime.parse(time.getStr("end"));

            LocalDateTime now = LocalDateTime.now();
            return calcDelayToNextWindow(now, weekdays, startTime, endTime);

        } catch (Exception e) {
            log.warn("[预警] time_range 解析失败，默认立即发送: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 从 now 出发，计算距离下一个合法时间窗口的毫秒数
     * weekdays[0]=周日 weekdays[1]=周一 ... weekdays[6]=周六
     */
    private long calcDelayToNextWindow(LocalDateTime now,
                                       int[] weekdays,
                                       LocalTime startTime,
                                       LocalTime endTime) {
        // 最多往后找 7 天
        for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
            LocalDateTime candidate = now.plusDays(dayOffset);
            // Java DayOfWeek: MON=1, TUE=2, ..., SUN=7
            // 映射到 weekdays 数组下标：SUN=0, MON=1, ..., SAT=6
            int dow = candidate.getDayOfWeek().getValue(); // 1=MON...7=SUN
            int idx = (dow % 7); // MON->1, TUE->2, ..., SAT->6, SUN->0

            if (weekdays[idx] != 1) continue; // 该天不在预警范围

            LocalTime candidateTime = dayOffset == 0
                    ? now.toLocalTime()
                    : startTime; // 未来某天从 start 开始

            if (dayOffset == 0) {
                // 今天：检查当前时间是否在窗口内
                if (!candidateTime.isBefore(startTime)
                        && !candidateTime.isAfter(endTime)) {
                    return 0; // 当前就在窗口内
                }
                // 今天窗口还没到
                if (candidateTime.isBefore(startTime)) {
                    return java.time.Duration.between(
                            now.toLocalTime(), startTime).toMillis();
                }
                // 今天窗口已过，继续找下一天
            } else {
                // 未来某天的 startTime
                LocalDateTime target = candidate.toLocalDate()
                        .atTime(startTime);
                return java.time.Duration.between(now, target).toMillis();
            }
        }
        // 7天内没找到（理论上不会），返回0兜底
        return 0;
    }
}