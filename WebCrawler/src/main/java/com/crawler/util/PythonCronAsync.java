package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.CrawlerCron;
import com.crawler.entity.NewsDataCron;
import com.crawler.mapper.CrawlerCronMapper;
import com.crawler.mapper.NewsDataCronMapper;
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
    private CrawlerCronMapper crawlerCronMapper;

    @Resource
    private NewsDataCronMapper newsDataCronMapper;

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
    public void callPythonAsync(CrawlerCron crawlerCron) {
        Integer crawlerId = crawlerCron.getCrawlerId();
        Long userId = crawlerCron.getUserId();
        log.info("[进程2] 启动，crawlerId={}", crawlerId);

        try {
            // Step2：构造请求体（根据 targetSource 选择接口）
            String apiPath;
            Map<String, Object> body = new HashMap<>();
            body.put("crawler_id", crawlerId);
            body.put("crawler_way", "cron");

            if ("integration".equals(crawlerCron.getTargetSource())) {
                apiPath = "/runIntegration";
            } else if ("xinhuanet".equals(crawlerCron.getTargetSource())) {
                apiPath = "/runXinHuaNet";
            } else {
                log.error("[进程2] 不支持的数据源: {}", crawlerCron.getTargetSource());
                updateState(crawlerCron, userId,-1);
                return;
            }

            // Step3：HTTP POST 调用 Python，阻塞等待返回
            log.info("[进程2] 调用Python接口 {}，crawlerId={}", apiPath, crawlerId);
            HttpResponse response = HttpRequest
                    .post(pythonBaseUrl + apiPath)
                    .body(JSONUtil.toJsonStr(body))
                    .contentType("application/json")
                    .timeout(httpTimeoutMs)
                    .execute();

            if (!response.isOk()) {
                log.error("[进程2] Python接口返回异常，status={}，crawlerId={}",
                        response.getStatus(), crawlerId);
                updateState(crawlerCron,userId, -1);
                return;
            }

            // Step4：解析返回值
            JSONObject responseBody = JSONUtil.parseObj(response.body());
            Integer code = responseBody.getInt("code");
            if (code == null || code != 1) {
                log.error("[进程2] Python业务返回失败，body={}，crawlerId={}",
                        response.body(), crawlerId);
                updateState(crawlerCron,userId, -1);
                return;
            }

            // Step6：解析 dataList
            JSONObject data = responseBody.getJSONObject("data");
            JSONArray dataList = data.getJSONArray("dataList");

            if (dataList == null || dataList.isEmpty()) {
                log.info("[进程2] dataList为空，crawlerId={}", crawlerId);
                updateState(crawlerCron,userId, 0);
                return;
            }

            // Step7：更新 state=3（数据保存中）
            updateState(crawlerCron,userId, 3);

            // Step8：转换并批量存入数据库
            List<NewsDataCron> newsList = parseDataList(dataList, crawlerId);
            int insertedCount = 0;
            if (!newsList.isEmpty()) {
                newsDataCronMapper.batchInsertIgnore(newsList);
                insertedCount = newsList.size();
                log.info("[进程2] 存入 {} 条新闻，crawlerId={}", insertedCount, crawlerId);
            }

            // Step9：预警判断（在 updateState 之前）
            checkAndSendAlert(crawlerCron, insertedCount);

            // Step10：更新 state=0（等待下次执行）,更新上次触发时间
            updateState(crawlerCron, userId, 0);
            crawlerCronMapper.updateLastTriggerTime(crawlerCron.getCrawlerId());

            log.info("[进程2] 完成，crawlerId={}", crawlerId);

        } catch (Exception e) {
            log.error("[进程2] 异常，crawlerId={}", crawlerId, e);
            updateState(crawlerCron,userId, -1);
        }
    }

    /**
     * 根据 frequency 判断是否触发预警
     */
    private void checkAndSendAlert(CrawlerCron crawlerCron, int insertedCount) {
        if (insertedCount <= 0) return;

        Integer threshold = crawlerCron.getAlertTrigger();

        if (threshold == null) {
            // null：有新增就直接预警
            alertUtil.sendAlertAsync(crawlerCron, insertedCount);
        } else {
            // 有值：累计达到阈值才预警
            crawlerCronMapper.addPendingCount(crawlerCron.getCrawlerId(), insertedCount);
            CrawlerCron latest = crawlerCronMapper.selectByCrawlerId(crawlerCron.getCrawlerId());
            int pendingCount = latest.getPendingCount() == null ? 0 : latest.getPendingCount();
            log.info("[预警判断] crawlerId={} pendingCount={} threshold={}",
                    crawlerCron.getCrawlerId(), pendingCount, threshold);
            if (pendingCount >= threshold) {
                alertUtil.sendAlertAsync(crawlerCron, pendingCount);
                crawlerCronMapper.resetPendingCount(crawlerCron.getCrawlerId());
            }
        }
    }

    private void updateState(CrawlerCron crawlerCron,Long userId,Integer state) {
        Map<String,Object> msg = new HashMap<>();
        msg.put("type","crawler_cron_state_change");
        msg.put("user_id",userId);
        msg.put("crawler_id",crawlerCron.getCrawlerId());
        msg.put("crawler_name",crawlerCron.getCrawlerName());
        msg.put("crawler_old_state",crawlerCron.getState());
        crawlerCronMapper.updateState(crawlerCron.getCrawlerId(), state);
        msg.put("crawler_new_state",state);
        VueSocketServer.sendToVue(userId.toString(),JSONUtil.toJsonStr(msg));
    }


    /**
     * 将 Python 返回的 dataList 转换为实体列表
     */
    private List<NewsDataCron> parseDataList(JSONArray dataList, Integer crawlerId) {
        List<NewsDataCron> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < dataList.size(); i++) {
            JSONObject item = dataList.getJSONObject(i);
            try {
                NewsDataCron news = new NewsDataCron();
                news.setCrawlerId(crawlerId);
                news.setTitle(item.getStr("title", ""));
                news.setContent(item.getStr("content", ""));
                news.setSource(item.getStr("source", ""));
                news.setUrl(item.getStr("url", ""));
                news.setPicUrl(item.getStr("picUrl", ""));

                // 解析发布时间，格式可能是 "2026-04-16" 或 "2026-04-16 10:30:00"
                String publishTimeStr = item.getStr("publishTime", "");
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
                if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                    result.add(news);
                }
            } catch (Exception e) {
                log.warn("[进程2] 解析第{}条新闻失败: {}", i, e.getMessage());
            }
        }
        return result;
    }

}