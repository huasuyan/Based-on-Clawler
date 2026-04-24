package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialReportSetting;
import com.crawler.mapper.SpecialReportSettingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ReportAsync {

    @Resource
    private SpecialReportSettingMapper specialReportSettingMapper;

    @Value("${crawler.cron.python-base-url:http://127.0.0.1:8088/api/python/crawler}")
    private String pythonCrawlerBaseUrl;

    @Value("${crawler.cron.http-timeout-ms:60000}")
    private int httpTimeoutMs;

    @Value("${http-readTimeout-ms}")
    private int httpReadTimeoutMs;


    /**
     * 异步执行一次报告专题任务：
     * 1. 调用Python /runIntegration 爬取并持久化数据
     * 2. 调用Python /generateReport 生成报告
     * 3. 更新执行状态和 lastExecuteTime
     * 4. 若为即时报告且已到结束时间，则停用专题
     */
    @Async
    public void updateDataAsync(SpecialReportSetting setting) {
        Long specialReportId = setting.getSpecialReportId();
        log.info("[报告任务] 爬虫更新启动，specialReportId={}", specialReportId);

        try {
            // Step1：更新 executeStatus=1（爬取数据中）
            specialReportSettingMapper.updateExecuteStatus(specialReportId, 1);

            // Step2：构造爬取请求，filter_time = 上次更新时间
            Date lastUpdateTime = setting.getLastUpdateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String filterTime = sdf.format(lastUpdateTime);
            Map<String, Object> crawlBody = new HashMap<>();
            crawlBody.put("task_id", specialReportId);
            crawlBody.put("task_way", "report");
            crawlBody.put("filter_time", filterTime);

            log.info("[报告任务] 调用爬取接口，specialReportId={}, filter_time={}",
                    specialReportId, filterTime);

            HttpResponse crawlResp = HttpRequest
                    .post(pythonCrawlerBaseUrl + "/crawler/runIntegration")
                    .body(JSONUtil.toJsonStr(crawlBody))
                    .contentType("application/json")
                    .timeout(httpTimeoutMs)
                    .setReadTimeout(httpReadTimeoutMs)
                    .execute();

            if (!crawlResp.isOk()) {
                log.error("[报告任务] 爬取接口返回异常，status={}, specialReportId={}",
                        crawlResp.getStatus(), specialReportId);
                specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
                return;}

            // 解析爬取结果
            Map<String, Object> crawlResult = JSONUtil.parseObj(crawlResp.body());
            Integer crawlCode = (Integer) crawlResult.get("code");
            if (crawlCode == null || crawlCode != 1) {
                log.error("[报告任务] 爬取业务失败，body={}, specialReportId={}",
                        crawlResp.body(), specialReportId);
                specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
                return;}
            // 更新 lastUpdateTime
            specialReportSettingMapper.updateLastUpdateTime(specialReportId, new Date());
        } catch (Exception e) {
            log.error("[报告任务] 异常，specialReportId={}", specialReportId, e);
            specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
        }
    }

    @Async
    public void runReportAsync(SpecialReportSetting setting) {
        Long specialReportId = setting.getSpecialReportId();
        log.info("[报告任务] 生成报告启动，specialReportId={}", specialReportId);

        try {
            // Step3：更新 executeStatus=2（生成报告中）
            specialReportSettingMapper.updateExecuteStatus(specialReportId, 2);

            // Step4：调用Python生成报告接口
            log.info("[报告任务] 调用生成报告接口，specialReportId={}", specialReportId);

            // Python报告接口是GET，拼接参数
            String reportUrl = pythonCrawlerBaseUrl
                    + "/report/generateReport?special_report_id=" + specialReportId;

            HttpResponse reportResp = HttpRequest
                    .get(reportUrl)
                    .timeout(httpTimeoutMs)
                    .execute();

            if (!reportResp.isOk()) {
                log.error("[报告任务] 生成报告接口返回异常，status={}, specialReportId={}",
                        reportResp.getStatus(), specialReportId);
                specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
                return;
            }

            Map<String, Object> reportResult = JSONUtil.parseObj(reportResp.body());
            Integer reportCode = (Integer) reportResult.get("code");
            if (reportCode == null || reportCode != 1) {
                log.error("[报告任务] 生成报告业务失败，body={}, specialReportId={}",
                        reportResp.body(), specialReportId);
                specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
                return;
            }

            // Step5：更新 lastExecuteTime 为当前时间
            specialReportSettingMapper.updateLastExecuteTime(specialReportId, new Date());

            // Step6：判断即时报告是否已到结束时间 → 停用
            if (setting.getReportType() == 1) {
                handleInstantReportFinish(setting);
            } else {
                // 定时报告：恢复为等待执行
                specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
            }

            log.info("[报告任务] 完成，specialReportId={}", specialReportId);

        } catch (Exception e) {
            log.error("[报告任务] 异常，specialReportId={}", specialReportId, e);
            specialReportSettingMapper.updateExecuteStatus(specialReportId, 0);
        }
    }

    /**
     * 即时报告：判断当前日期是否已到或超过 end_date
     * 若是，则将 executeStatus=3（已完成）并 statusEnabled=0（停用）
     */
    private void handleInstantReportFinish(SpecialReportSetting setting) {
        try {
            Map<String, Object> typeParams = JSONUtil.parseObj(
                    setting.getTypeParams().toString());
            String endDateStr = (String) typeParams.get("end_date");
            if (endDateStr == null) {
                specialReportSettingMapper.updateExecuteStatus(
                        setting.getSpecialReportId(), 0);
                return;
            }
            LocalDate endDate = LocalDate.parse(endDateStr);
            // 今天 >= 结束日期，任务结束
            if (!LocalDate.now().isBefore(endDate)) {
                specialReportSettingMapper.updateExecuteStatus(
                        setting.getSpecialReportId(), 3);
                specialReportSettingMapper.updateStatusEnabled(
                        setting.getSpecialReportId(), 0);
                log.info("[报告任务] 即时报告已到结束日期，自动停用，specialReportId={}",
                        setting.getSpecialReportId());
            } else {
                specialReportSettingMapper.updateExecuteStatus(
                        setting.getSpecialReportId(), 0);
            }
        } catch (Exception e) {
            log.warn("[报告任务] 解析即时报告结束时间失败，specialReportId={}",
                    setting.getSpecialReportId(), e);
            specialReportSettingMapper.updateExecuteStatus(
                    setting.getSpecialReportId(), 0);
        }
    }
}
