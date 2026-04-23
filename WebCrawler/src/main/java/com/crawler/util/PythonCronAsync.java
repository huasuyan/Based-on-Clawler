package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.mapper.SpecialAlertSettingMapper;
import com.crawler.mapper.NewsDataMapper;
import com.crawler.websockets.VueSocketServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class PythonCronAsync {

    @Resource
    private SpecialAlertSettingMapper specialAlertSettingMapper;

    @Resource
    private NewsDataMapper newsDataMapper;

    @Resource
    private AlertUtil alertUtil;

    @Value("${crawler.cron.python-base-url:http://127.0.0.1:8088/api/python/crawler}")
    private String pythonBaseUrl;

    @Value("${crawler.cron.http-timeout-ms:60000}")
    private int httpTimeoutMs;

    /**
     * 异步执行一次预警专题爬取：
     * 1. 调用Python接口，
     * 3. WebSocket推送前端
     * 注意：定时触发逻辑由 CrawlerCronScheduler 负责，此方法只执行一次。
     */
    @Async
    public void callPythonAsync(SpecialAlertSetting specialAlertSetting) {
        Integer alertId = specialAlertSetting.getAlertId();
        Long userId = specialAlertSetting.getUserId();
        //更新上次触发时间
        specialAlertSettingMapper.updateLastTriggerTime(alertId);
        log.info("[进程2] 启动，alertId={}", alertId);

        try {
            // Step1：更新 state=1（爬取数据中）
            updateState(specialAlertSetting, userId, 1);

            // Step2：构造请求体，filter_time 取 latest_news_time
            Map<String, Object> body = new HashMap<>();
            body.put("task_id", alertId);
            body.put("task_way", "alert");

            // latest_news_time 不为空则传入，作为Python侧过滤基准
            Date latestNewsTime = specialAlertSetting.getLatestNewsTime();
            if (latestNewsTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                body.put("filter_time", sdf.format(latestNewsTime));
            }

            // Step3：HTTP POST 调用 Python，阻塞等待返回
            log.info("[进程2] 调用Python接口，alertId={}, filter_time={}",
                    alertId, body.get("filter_time"));

            HttpResponse response = HttpRequest
                    .post(pythonBaseUrl + "/runIntegration")
                    .body(JSONUtil.toJsonStr(body))
                    .contentType("application/json")
                    .timeout(httpTimeoutMs)
                    .execute();

            if (!response.isOk()) {
                log.error("[进程2] Python接口返回异常，status={}，alertId={}",
                        response.getStatus(), alertId);
                updateState(specialAlertSetting, userId, -1);
                return;
            }

            // Step4：解析返回值
            JSONObject responseBody = JSONUtil.parseObj(response.body());
            Integer code = responseBody.getInt("code");
            if (code == null || code != 1) {
                log.error("[进程2] Python业务返回失败，body={}，alertId={}",
                        response.body(), alertId);
                updateState(specialAlertSetting, userId, -1);
                return;
            }

            // Step5：Python已完成持久化，查询本次实际新增条数
            // 通过对比 latest_news_time 前后的数据量来获取新增数
            int insertedCount = 0;
            if (latestNewsTime != null) {
                insertedCount = newsDataMapper.countNewsByAlertIdAfterTime(
                        alertId, latestNewsTime);
            } else {
                insertedCount = newsDataMapper.countNewsByAlertId(alertId);
            }
            log.info("[进程2] Python持久化完成，实际新增 {} 条，alertId={}", insertedCount, alertId);

            // Step6：预警判断
            checkAndSendAlert(specialAlertSetting, insertedCount);

            // Step7：更新 state=0（等待下次执行），更新上次触发时间
            updateState(specialAlertSetting, userId, 0);

            log.info("[进程2] 完成，alertId={}", alertId);

        } catch (Exception e) {
            log.error("[进程2] 异常，alertId={}", alertId, e);
            updateState(specialAlertSetting, userId, -1);
        }
    }

    /**
     * 根据 alert_trigger 判断是否触发预警
     */
    private void checkAndSendAlert(SpecialAlertSetting specialAlertSetting, int insertedCount) {
        if (insertedCount <= 0) return;

        Integer threshold = specialAlertSetting.getAlertTrigger();

        if (threshold == null) {
            // null：有新增就直接预警
            alertUtil.sendAlertAsync(specialAlertSetting, insertedCount);
        } else {
            // 有值：累计达到阈值才预警
            specialAlertSettingMapper.addPendingCount(specialAlertSetting.getAlertId(), insertedCount);
            SpecialAlertSetting latest = specialAlertSettingMapper.selectByAlertId(specialAlertSetting.getAlertId());
            int pendingCount = latest.getPendingCount() == null ? 0 : latest.getPendingCount();
            log.info("[预警判断] alertId={} pendingCount={} threshold={}",
                    specialAlertSetting.getAlertId(), pendingCount, threshold);
            if (pendingCount >= threshold) {
                alertUtil.sendAlertAsync(specialAlertSetting, pendingCount);
                specialAlertSettingMapper.resetPendingCount(specialAlertSetting.getAlertId());
            }
        }
    }

    private void updateState(SpecialAlertSetting specialAlertSetting, Long userId, Integer state) {
        Map<String,Object> msg = new HashMap<>();
        msg.put("type","alert_cron_state_change");
        msg.put("user_id",userId);
        msg.put("alert_id", specialAlertSetting.getAlertId());
        msg.put("alert_name", specialAlertSetting.getAlertName());
        msg.put("alert_old_state", specialAlertSetting.getState());
        specialAlertSettingMapper.updateState(specialAlertSetting.getAlertId(), state);
        msg.put("alert_new_state",state);
        VueSocketServer.sendToVue(userId.toString(),JSONUtil.toJsonStr(msg));
    }

}