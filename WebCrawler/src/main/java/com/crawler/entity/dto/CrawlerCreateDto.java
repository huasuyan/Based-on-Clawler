package com.crawler.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 创建并执行任务 请求体
 */
@Data
public class CrawlerCreateDto {
    private Long userId;         // 用户ID（从token解析）
    private String crawlerName;     // 任务名
    private String targetSource;    // 数据源
    private String keyWord;         // 关键词
    private Map<String, Object> params; // 可变参数
}