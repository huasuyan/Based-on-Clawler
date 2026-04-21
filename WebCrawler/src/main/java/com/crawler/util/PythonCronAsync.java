package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.CrawlerCron;
import com.crawler.mapper.CrawlerCronMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PythonCronAsync {
    @Resource
    private CrawlerCronMapper crawlerCronMapper;

    /** Python 后端地址，对应 application.yml: crawler.cron.python-base-url */
    @Value("${crawler.cron.python-base-url:http://127.0.0.1:8088/api/python/crawler}")
    private String pythonBaseUrl;

    /** HTTP 超时时间（毫秒），根据爬虫任务耗时适当调大 */
    @Value("${crawler.cron.http-timeout-ms:60000}")
    private int httpTimeoutMs;

    @Value("${crawler.cron.result-root-path:/crawlerCronResult}")
    private String resultRootPath;
    /**
     * 进程2：异步 HTTP 调用 Python 爬虫接口，阻塞等待返回值，
     * 拿到结果后写入本地 JSON 文件，并更新数据库状态。
     *
     * 使用 @Async 在独立线程池中执行，不阻塞主线程。
     * 需要在启动类或配置类上加 @EnableAsync 才能生效。
     *
     * Python 接口约定：
     *   POST {pythonBaseUrl}/crawlerCron/run
     *   RequestBody: 专题参数 JSON
     *   ResponseBody: {"code":1,"data":[{...},{...}]}
     */
    @Async
    public void callPythonAsync(CrawlerCron crawlerCron) {
        Integer crawlerId = crawlerCron.getCrawlerId();
        log.info("[进程2] 启动，crawlerId={}", crawlerId);

        // ── Step1：更新 DB state = 1（爬取数据中）
        crawlerCronMapper.updateState(crawlerId, 1);

        try {
            Map<String, Object> body = new HashMap<>();
            // 针对 integration 数据源，构造请求体，调用runIntegration接口
            if(crawlerCron.getTargetSource().equals("integration")){
                // ── Step2：构造请求体
                body.put("keyWord",      JSONUtil.parseObj(crawlerCron.getKeyWord()));
                body.put("params",       crawlerCron.getParams() != null
                        ? JSONUtil.parseObj(crawlerCron.getParams()) : null);
                body.put("timeRange",    crawlerCron.getTimeRange() != null
                        ? JSONUtil.parseObj(crawlerCron.getTimeRange()) : null);

                // ── Step3：HTTP POST，阻塞等待 Python 返回
                log.info("[进程2] 调用Python接口，crawlerId={}", crawlerId);
                HttpResponse response = HttpRequest
                        .post(pythonBaseUrl + "/runIntegration")
                        .body(JSONUtil.toJsonStr(body))
                        .contentType("application/json")
                        .timeout(httpTimeoutMs)
                        .execute();

                if (!response.isOk()) {
                    log.error("[进程2] Python接口返回异常，status={}，crawlerId={}",
                            response.getStatus(), crawlerId);
                    crawlerCronMapper.updateState(crawlerId, -1);
                    return;
                }

                // ── Step4：解析返回值
                Map<String, Object> responseBody = JSONUtil.parseObj(response.body());
                Integer code = (Integer) responseBody.get("code");
                if (code == null || code != 1) {
                    log.error("[进程2] Python业务返回失败，body={}，crawlerId={}",
                            response.body(), crawlerId);
                    crawlerCronMapper.updateState(crawlerId, -1);
                    return;
                }

                // ── Step5：更新 DB state = 2（数据清洗中，由Python完成）
                // Python 返回的已是清洗后数据，此处直接标记为保存中
                crawlerCronMapper.updateState(crawlerId, 3);

                // 将清洗后的数据写入数据库

                // ── Step6：更新 DB state = 0（等待下一次执行）
                crawlerCronMapper.updateState(crawlerId, 0);
                log.info("[进程2] 完成，crawlerId={}", crawlerId);
            }
        } catch (Exception e) {
            log.error("[进程2] 异常，crawlerId={}", crawlerId, e);
            // 写入失败状态，等待下一次执行
            crawlerCronMapper.updateState(crawlerId, -1);
        }
    }
}
