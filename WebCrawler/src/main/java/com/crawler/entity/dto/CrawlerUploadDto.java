package com.crawler.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerUploadDto {
    private Long userId; // 用户ID
    private String crawlerName; // 爬虫名称
    private String jobDesc; // 任务描述
    private String scheduleType; // 执行方式
    private String scheduleConf; // CRON表达式
    private String crawlerSource; // Glue脚本内容
}