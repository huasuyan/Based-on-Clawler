package com.crawler.entity.dto;

import lombok.Data;

@Data
public class CrawlerPageQueryDTO {
    // 基础分页参数
    private Long userId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    // 动态查询条件（可选）
    private String crawlerName;
    private String scheduleType;
    private String configMethod;
    private Integer triggerStatus;
}