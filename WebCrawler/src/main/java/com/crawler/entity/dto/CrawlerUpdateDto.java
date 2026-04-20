package com.crawler.entity.dto;

import lombok.Data;

@Data
public class CrawlerUpdateDto {
    // 基础参数
    private Long userId;
    private Integer CrawlerId;

    // 动态条件（可选）
    private String crawlerName;
    private String scheduleType;
    private String scheduleConf;
    private Integer configMethod;
    private Integer triggerStatus;
    private String crawlerSource;
}