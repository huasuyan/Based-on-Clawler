package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.NewsData;
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
     * 1. 调用Python接口
     * 2. 解析结果存入 news_data_cron
     * 3. WebSocket推送前端
     * 注意：定时触发逻辑由 CrawlerCronScheduler 负责，此方法只执行一次。
     */
    @Async
    public void callPythonAsync(SpecialAlertSetting specialAlertSetting) {
        Integer alertId = specialAlertSetting.getAlertId();
        Long userId = specialAlertSetting.getUserId();
        log.info("[进程2] 启动，alertId={}", alertId);

        try {
            // Step2：构造请求体（根据 targetSource 选择接口）
            String apiPath;
            Map<String, Object> body = new HashMap<>();
            body.put("task_id", alertId);
            body.put("task_way", "alert");

            if ("integration".equals(specialAlertSetting.getTargetSource())) {
                apiPath = "/runIntegration";
            } else if ("xinhuanet".equals(specialAlertSetting.getTargetSource())) {
                apiPath = "/runXinHuaNet";
            } else {
                log.error("[进程2] 不支持的数据源: {}", specialAlertSetting.getTargetSource());
                updateState(specialAlertSetting, userId,-1);
                return;
            }

            // Step3：HTTP POST 调用 Python，阻塞等待返回
            log.info("[进程2] 调用Python接口 {}，alertId={}", apiPath, alertId);
            HttpResponse response = HttpRequest
                    .post(pythonBaseUrl + apiPath)
                    .body(JSONUtil.toJsonStr(body))
                    .contentType("application/json")
                    .timeout(httpTimeoutMs)
                    .execute();

            if (!response.isOk()) {
                log.error("[进程2] Python接口返回异常，status={}，alertId={}",
                        response.getStatus(), alertId);
                updateState(specialAlertSetting,userId, -1);
                return;
            }

            // Step4：解析返回值
            JSONObject responseBody = JSONUtil.parseObj(response.body());
            Integer code = responseBody.getInt("code");
            if (code == null || code != 1) {
                log.error("[进程2] Python业务返回失败，body={}，alertId={}",
                        response.body(), alertId);
                updateState(specialAlertSetting,userId, -1);
                return;
            }

            // Step6：解析 dataList
            JSONObject data = responseBody.getJSONObject("data");
            JSONArray dataList = data.getJSONArray("dataList");

            if (dataList == null || dataList.isEmpty()) {
                log.info("[进程2] dataList为空，alertId={}", alertId);
                updateState(specialAlertSetting,userId, 0);
                return;
            }

            // Step7：更新 state=3（数据保存中）
            updateState(specialAlertSetting,userId, 3);

            // Step8：转换并批量存入数据库
            List<NewsData> newsList = parseDataList(dataList);
            int insertedCount = 0;
            if (!newsList.isEmpty()) {
                newsDataMapper.batchInsertIgnore(newsList);
                insertedCount = newsList.size();
                log.info("[进程2] 存入 {} 条新闻，alertId={}", insertedCount, alertId);
            }

            // Step9：预警判断（在 updateState 之前）
            checkAndSendAlert(specialAlertSetting, insertedCount);

            // Step10：更新 state=0（等待下次执行）,更新上次触发时间
            updateState(specialAlertSetting, userId, 0);
            specialAlertSettingMapper.updateLastTriggerTime(specialAlertSetting.getAlertId());

            log.info("[进程2] 完成，alertId={}", alertId);

        } catch (Exception e) {
            log.error("[进程2] 异常，alertId={}", alertId, e);
            updateState(specialAlertSetting,userId, -1);
        }
    }

    /**
     * 根据 frequency 判断是否触发预警
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


    /**
     * 将 Python 返回的 dataList 转换为实体列表
     */
    private List<NewsData> parseDataList(JSONArray dataList) {
        List<NewsData> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < dataList.size(); i++) {
            JSONObject item = dataList.getJSONObject(i);
            try {
                NewsData news = new NewsData();
                news.setTitle(item.getStr("title", ""));
                news.setContent(item.getStr("content", ""));
                news.setVideo(item.getStr("video", ""));
                news.setPlatform(item.getStr("platform", ""));
                news.setSource(item.getStr("source", ""));
                news.setPublisher(item.getStr("publisher", ""));
                news.setComment(item.getInt("comment", 0));
                news.setRegion(item.getStr("region", ""));
                news.setOriginalUrl(item.getStr("original_url", ""));
                news.setArticleType(item.getStr("article_type", ""));
                news.setSourceUrl(item.getStr("source_url", ""));
                news.setAlertId(item.getLong("alert_id"));

                // 解析发布时间，格式可能是 "2026-04-16" 或 "2026-04-16 10:30:00"
                String publishTimeStr = item.getStr("publish_time", "");
                if (publishTimeStr != null && !publishTimeStr.isEmpty()) {
                    try {
                        if (publishTimeStr.length() == 10) {
                            publishTimeStr += " 00:00:00";
                        }
                        news.setPublishTime(sdf.parse(publishTimeStr));
                    } catch (Exception ex) {
                        news.setPublishTime(new Date()); // 解析失败用当前时间
                    }
                } else {
                    news.setPublishTime(new Date());
                }

                // url不能为空（联合主键）
                if (news.getOriginalUrl() != null && !news.getOriginalUrl().isEmpty()) {
                    result.add(news);
                }
            } catch (Exception e) {
                log.warn("[进程2] 解析第{}条新闻失败: {}", i, e.getMessage());
            }
        }
        return result;
    }

}