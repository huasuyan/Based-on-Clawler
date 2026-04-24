package com.crawler.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertTabulate {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 预警专题ID
     */
    private Long alertId;

    /**
     * 文章总数
     */
    private Integer totalArticle;

    /**
     * 敏感总数
     */
    private Integer totalSensitive;

    /**
     * 中性总数
     */
    private Integer totalNeutral;

    /**
     * 非敏感总数
     */
    private Integer totalNonSensitive;

    /**
     * 地域分布图表JSON数据
     */
    private String regionChart;

    /**
     * 地域发布量排名表格JSON数据
     */
    private String regionRank;

    /**
     * 统计创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}