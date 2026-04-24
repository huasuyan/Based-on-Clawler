package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.CrawlerNoneCreate;
import com.crawler.entity.NewsDataNone;
import com.crawler.mapper.CrawlerNoneMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AsyncTaskUtil {

    @Autowired
    private CrawlerNoneMapper crawlerNoneMapper;

    @Value("${python.url:}")
    private String pythonUrl;

    @Async
    public void asyncRunTask(Integer crawlerId) {

        if (crawlerId == null) {
            log.error("任务ID为null，无法执行异步任务");
            return;
        }

        CrawlerNoneCreate task = crawlerNoneMapper.selectById(crawlerId);

        try {
            // 状态1：爬取数据中
            updateState(crawlerId, 1);
            List<NewsDataNone> dataList = doPostData(crawlerId);

            // 状态2：数据清洗中
            updateState(crawlerId, 2);

            // 状态3：数据保存中
            updateState(crawlerId, 3);
            // 调用Python后端接口获取数据
            crawlerNoneMapper.insertData(dataList);

            // 状态4：任务完成
            updateState(crawlerId, 4);
            task.setFinishTime(LocalDateTime.now());
            crawlerNoneMapper.updateById(task);

        } catch (Exception e) {
            // 状态-1：任务失败
            updateState(crawlerId, -1);
            log.error("异步任务执行失败，任务ID: {}", crawlerId, e);
        }
    }

    private void updateState(Integer crawlerId, int state) {
        CrawlerNoneCreate task = new CrawlerNoneCreate();
        task.setCrawlerId(crawlerId);
        task.setState(state);
        crawlerNoneMapper.updateById(task);
    }

    List<NewsDataNone> doPostData(Integer crawlerId) {
        try {
            // 构建请求参数
            String crawlerWay = "none";
            Map<String, Object> params = new HashMap<>();
            params.put("crawler_id", crawlerId);
            params.put("crawler_way", crawlerWay);

            // 构建HTTP请求
            HttpRequest request = HttpRequest.post(pythonUrl)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params));

            // 执行请求
            HttpResponse response = request.execute();

            // 检查响应状态
            if (!response.isOk()) {
                log.error("Python接口调用失败，状态码: {}", response.getStatus());
                throw new RuntimeException("Python接口调用失败，状态码: " + response.getStatus());
            }

            // 解析响应数据
            String responseBody = response.body();
            log.info("Python接口返回数据: {}", responseBody);

            // 解析JSON响应
            Map<String, Object> responseMap = JSONUtil.parseObj(responseBody);
            Integer code = (Integer) responseMap.get("code");
            String msg = (String) responseMap.get("msg");

            if (code != 1 || !"success".equals(msg)) {
                log.error("Python接口返回错误: code={}, msg={}", code, msg);
                throw new RuntimeException("Python接口返回错误: " + msg);
            }

            // 获取data部分
            Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
            if (dataMap == null) {
                log.warn("Python接口返回的data为null");
                return new ArrayList<>();
            }

            // 获取dataList
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataMap.get("dataList");
            if (dataList == null || dataList.isEmpty()) {
                log.warn("Python接口返回的dataList为空");
                return new ArrayList<>();
            }
            // 解析响应数据
            List<NewsDataNone> newsDataNoneList = new ArrayList<>();
            for (Map<String, Object> item : dataList) {
                NewsDataNone newsDataNone = new NewsDataNone();
                newsDataNone.setCrawlerId(crawlerId); // 设置任务ID
                newsDataNone.setTitle((String) item.get("title"));
                newsDataNone.setContent((String) item.get("content"));
                newsDataNone.setPublishTime((String) item.get("publishTime"));
                newsDataNone.setSource((String) item.get("source"));
                newsDataNone.setUrl((String) item.get("url"));
                newsDataNone.setPicUrl((String) item.get("picUrl"));
                newsDataNoneList.add(newsDataNone);
            }
            return newsDataNoneList;
        } catch (Exception e) {
            log.error("调用Python接口失败", e);
            throw new RuntimeException("调用Python接口失败: " + e.getMessage(), e);
        }
    }
}