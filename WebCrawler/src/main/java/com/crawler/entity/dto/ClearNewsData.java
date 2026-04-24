package com.crawler.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ClearNewsData {
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
    private Integer sensitivityLevel;// 敏感度等级:0=普通、1=低敏感、2=中敏感、3=高敏感
    private String sensitivityLabel; // 敏感标签分类(政治、民生、金融、负面、谣言等)
    private Integer sentimentType;   // 情绪极性:-1=负面、0=中性、1=正面
}
