package com.crawler.entity.dto.alertMap;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertMapDto {
    // 坐标表主键
    private Long id;
    // 预警专题信息
    private Long alertId;
    private String alertName;
    private Integer alertLevel;
    // 新闻信息
    private Long newsId;
    private String newsTitle;
    private String newsContent;
    private String newsUrl;
    // 经纬度坐标
    private BigDecimal longitude;
    private BigDecimal latitude;
    // 预警日期
    private LocalDateTime alertDate;
}
