package com.crawler.entity.dto.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 舆情监测文章响应DTO
 * 用于 /article 和 /infoList 接口返回文章数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorArticleDto {
    private String title;           // 文章标题
    private String content;         // 文章内容
    private String publishTime;     // 发布时间，格式：yyyy-MM-dd
    private String source;          // 文章来源
    private String url;             // 原文链接
    private String picUrl;          // 图片链接
}
