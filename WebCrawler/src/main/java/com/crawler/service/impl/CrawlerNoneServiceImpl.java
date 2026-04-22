package com.crawler.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.crawler.entity.CrawlerNoneCreate;
import com.crawler.entity.CrawlerNoneResult;
import com.crawler.entity.NewsDataNone;
import com.crawler.entity.dto.CrawlerCreateDto;
import com.crawler.mapper.CrawlerNoneMapper;
import com.crawler.service.CrawlerNoneService;
import com.crawler.util.AsyncTaskUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrawlerNoneServiceImpl implements CrawlerNoneService {

    @Autowired
    private CrawlerNoneMapper crawlerNoneMapper;
    @Autowired
    private AsyncTaskUtil asyncTaskUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer createAndRun(CrawlerCreateDto crawler) {
        // 1. 保存任务到数据库，状态=已创建(0)
        CrawlerNoneCreate task = new CrawlerNoneCreate();
        task.setUserId(crawler.getUserId());
        task.setCrawlerName(crawler.getCrawlerName());
        task.setState(0); // 状态0
        task.setTargetSource(crawler.getTargetSource());
        task.setKeyWord(crawler.getKeyWord());
        task.setParams(JSONUtil.toJsonStr(crawler.getParams()));
        task.setCreateTime(LocalDateTime.now());

        crawlerNoneMapper.insert(task);
        // 2. 异步执行任务
        asyncTaskUtil.asyncRunTask(task.getCrawlerId());

        if (task.getCrawlerId() == null) {
            throw new RuntimeException("任务ID为空");
        }
        return task.getCrawlerId();
    }

    /**
     * 查询任务结果
     */
    public CrawlerNoneResult getTaskResult(Integer crawlerId) {
        CrawlerNoneCreate task = crawlerNoneMapper.selectById(crawlerId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        List<NewsDataNone> dataList = crawlerNoneMapper.selectData(crawlerId);

        // 组装返回对象
        CrawlerNoneResult resp = new CrawlerNoneResult();
        resp.setCrawlerId(task.getCrawlerId());
        resp.setUserId(Math.toIntExact(task.getUserId()));
        resp.setCrawlerName(task.getCrawlerName());
        resp.setState(task.getState()); // 数字状态
        resp.setTargetSource(task.getTargetSource());
        resp.setKeyWord(task.getKeyWord());
        resp.setParams(JSONUtil.parseObj(task.getParams()));
        resp.setCreateTime(task.getCreateTime());
        resp.setFinishTime(task.getFinishTime());
        resp.setDataList(dataList);
        return resp;
    }

    public void export(Integer crawlerId, String format, HttpServletResponse response) {
        // 1. 校验任务状态是否已完成（状态5）
        CrawlerNoneCreate task = crawlerNoneMapper.selectById(crawlerId);
        if (task.getState() != 4) {
            throw new RuntimeException("任务未完成，无法导出");
        }

        // 2. 读取数据
        List<NewsDataNone> dataList = crawlerNoneMapper.selectData(crawlerId);

        // 3. 按格式导出
        switch (format.toLowerCase()) {
            case "excel":
                exportExcel(dataList, response, task);
                break;
            case "csv":
                exportCsv(dataList, response, task);
                break;
            case "json":
                exportJson(dataList, response, task);
                break;
            default:
                throw new RuntimeException("不支持的导出格式");
        }
    }

    private void setResponseHeader(HttpServletResponse response, String fileName, String contentType, long contentLength) throws IOException {
        String encodeName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encodeName);
        response.setHeader("Cache-Control", "no-cache");
        if (contentLength > 0) {
            response.setContentLengthLong(contentLength);
        }
    }


    private void exportExcel(List<NewsDataNone> dataList, HttpServletResponse response, CrawlerNoneCreate task) {
        try {
            String fileName = "舆情任务_" + task.getCrawlerId() + ".xlsx";
            setResponseHeader(response, fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", dataList.size());
            EasyExcel.write(response.getOutputStream(), NewsDataNone.class)
                    .sheet("舆情数据")
                    .doWrite(dataList);
        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败", e);
        }
    }

    private void exportCsv(List<NewsDataNone> dataList, HttpServletResponse response, CrawlerNoneCreate task) {
        try {
            String fileName = "舆情任务_" + task.getCrawlerId() + ".csv";
            setResponseHeader(response, fileName, "text/csv", dataList.size());
            PrintWriter writer = response.getWriter();
            writer.println("标题,内容,发布时间,来源,情感倾向,链接");
            for (NewsDataNone data : dataList) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        data.getTitle(), data.getContent(), data.getPublishTime(),
                        data.getSource(), data.getUrl(), data.getPicUrl());
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("CSV导出失败", e);
        }
    }

    private void exportJson(List<NewsDataNone> dataList, HttpServletResponse response, CrawlerNoneCreate task) {
        try {
            String fileName = "舆情任务_" + task.getCrawlerId() + ".json";
            setResponseHeader(response, fileName, "application/json", dataList.size());
            PrintWriter writer = response.getWriter();
            writer.println(JSONUtil.toJsonStr(dataList));
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("JSON导出失败", e);
        }
    }


}
