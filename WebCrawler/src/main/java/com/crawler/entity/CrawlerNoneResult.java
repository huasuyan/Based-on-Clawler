package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerNoneResult {
    private Integer crawlerId;
    private Integer userId;
    private String crawlerName;
    private Integer state; // 任务状态（数字）
    private String targetSource;
    private String keyWord;
    private Map<String, Object> params;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    private List<NewsDataNone> dataList; // 舆情数据列表
    // 舆情数据子对象
}