package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 原始爬取数据实体
 * 对应数据库表 news_data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsData {
    private Long newsId;            // 数据唯一ID（主键自增）
    private Long specialReportId;   // 数据对应的舆情报告专题ID，默认0
    private Long alertId;           // 数据对应的预警专题ID，默认0
    private String title;           // 标题（非空）
    private String content;         // 内容（longtext，可为null）
    private String video;           // 视频链接（可为null）
    private String platform;        // 平台（可为null）
    private String source;          // 来源（可为null）
    private String publisher;       // 发布人（可为null）
    private Date publishTime;       // 发布时间（非空）
    private Integer comment;        // 评论数，默认0
    private String region;          // 地域（可为null）
    private String originalUrl;     // 原文地址（非空）
    private String articleType;     // 发文类型（可为null）
    private String sourceUrl;       // 来源地址（可为null）
    private Date createTime;        // 入库时间，默认CURRENT_TIMESTAMP

    public NewsData(NewsData c){
        this.newsId = c.getNewsId();
        this.specialReportId = c.getSpecialReportId();
        this.alertId    = c.getAlertId();
        this.title  = c.getTitle();
        this.content = c.getContent();
        this.video = c.getVideo();
        this.platform = c.getPlatform();
        this.source    = c.getSource();
        this.publisher = c.getPublisher();
        this.publishTime = c.getPublishTime();
        this.comment    = c.getComment();
        this.region    = c.getRegion();
        this.originalUrl = c.getOriginalUrl();
        this.articleType = c.getArticleType();
        this.sourceUrl = c.getSourceUrl();
        this.createTime = c.getCreateTime();
    }
}