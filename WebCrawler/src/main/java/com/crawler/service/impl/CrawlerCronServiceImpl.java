package com.crawler.service.impl;

import com.crawler.entity.CrawlerCron;
import com.crawler.entity.NewsDataCron;
import com.crawler.entity.Result;
import com.crawler.entity.dto.*;
import com.crawler.mapper.CrawlerCronMapper;
import com.crawler.mapper.NewsDataCronMapper;
import com.crawler.service.CrawlerCronService;
import com.crawler.util.PythonCronAsync;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CrawlerCronServiceImpl implements CrawlerCronService {

    @Resource
    private CrawlerCronMapper crawlerCronMapper;

    @Resource
    private NewsDataCronMapper newsDataCronMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    // 列表查询
    @Override
    public Map<String, Object> pageList(CrawlerCronPageQueryDto queryDto) {
        List<CrawlerCronDto> crawlerInfo = crawlerCronMapper.pageList(queryDto)
                .stream()
                .map(CrawlerCronDto::new)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("crawlerInfo", crawlerInfo);
        return result;
    }

    // 新增
    @Override
    public Map<String, Object> create(CrawlerCronCreateDto createDto) {
        CrawlerCron crawlerCron = new CrawlerCron();
        crawlerCron.setUserId(createDto.getUserId());
        crawlerCron.setCrawlerName(createDto.getCrawlerName());
        crawlerCron.setTargetSource(createDto.getTargetSource());
        crawlerCron.setKeyWord(createDto.getKeyWord());
        crawlerCron.setParams(createDto.getParams());
        crawlerCron.setFrequency(createDto.getFrequency());
        crawlerCron.setAlertTrigger(createDto.getAlertTrigger());
        crawlerCron.setTimeRange(createDto.getTimeRange());
        crawlerCron.setAlertMethod(createDto.getAlertMethod());
        crawlerCron.setDedupEnable(createDto.getDedupEnable());

        crawlerCronMapper.insert(crawlerCron);  // useGeneratedKeys，crawlerId 回填

        Map<String, Object> result = new HashMap<>();
        result.put("crawlerId", crawlerCron.getCrawlerId());
        return result;
    }

    // 编辑（专题须处于关闭状态）
    @Override
    public Map<String, Object> edit(CrawlerCronEditDto editDto) {
        CrawlerCron existing = crawlerCronMapper.selectByCrawlerId(editDto.getCrawlerId());
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }
        if (existing.getTriggerState() == 1) {
            throw new RuntimeException("请先关闭预警专题后再编辑");
        }

        CrawlerCron update = new CrawlerCron();
        update.setCrawlerId(editDto.getCrawlerId());
        update.setCrawlerName(editDto.getCrawlerName());
        update.setTargetSource(editDto.getTargetSource());
        update.setKeyWord(editDto.getKeyWord());
        update.setParams(editDto.getParams());
        update.setFrequency(editDto.getFrequency());
        update.setAlertTrigger(editDto.getAlertTrigger());
        update.setTimeRange(editDto.getTimeRange());
        update.setAlertMethod(editDto.getAlertMethod());
        update.setDedupEnable(editDto.getDedupEnable());

        crawlerCronMapper.update(update);

        Map<String, Object> result = new HashMap<>();
        result.put("crawlerId", editDto.getCrawlerId());
        return result;
    }

    // ----------------------------------------------------------------
    //  启用 / 关闭（异步 HTTP 调用 Python）
    // ----------------------------------------------------------------

    @Override
    public Map<String, Object> toggleTriggerState(Integer crawlerId) {
        CrawlerCron existing = crawlerCronMapper.selectByCrawlerId(crawlerId);
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }

        Map<String, Object> result = new HashMap<>();
        if (existing.getTriggerState() == 0) {
            // ── 当前关闭 → 启用 ──────────────────────────────────────
            // 1. 更新 DB，主线程立即返回前端
            crawlerCronMapper.updateTriggerState(crawlerId, 1);
            // 2. 开启进程2：异步 HTTP 调用 Python，等待结果后写文件
            pythonCronAsync.callPythonAsync(existing);
            result.put("triggerState", 1);
        } else {
            // ── 当前启用 → 关闭 ──────────────────────────────────────
            // 只更新 DB，Python 侧定时任务自然停止（不再被调用）
            crawlerCronMapper.updateTriggerState(crawlerId, 0);
            crawlerCronMapper.updateState(crawlerId, 0);
            result.put("triggerState", 0);
        }
        return result;
    }

    // 删除专题（须处于关闭状态）
    @Override
    public Result delete(Integer crawlerId) {
        CrawlerCron existing = crawlerCronMapper.selectByCrawlerId(crawlerId);
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }
        if (existing.getTriggerState() == 1) {
            throw new RuntimeException("请先关闭预警专题后再删除");
        }
        crawlerCronMapper.deleteByCrawlerId(crawlerId);
        return Result.success();
    }

    // 舆情消息列表---待修改逻辑
    @Override
    public Map<String, Object> infoList(CrawlerCronInfoDto queryDto) {
        List<NewsDataCron> newsData = newsDataCronMapper.infoList(queryDto)
                .stream()
                .map(NewsDataCron::new)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("NewsDataList", newsData);
        return result;
    }

    // 删除舆情消息
    @Override
    public Result infoDelete(Integer crawlerId, String url) {
        NewsDataCron existing = newsDataCronMapper.select(url,crawlerId);
        if (existing == null) {
            throw new RuntimeException("舆情消息不存在");
        }
        newsDataCronMapper.delete(url,crawlerId);
        return Result.success();
    }

}
