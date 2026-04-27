package com.crawler.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertMapCoordinate {

    /**
     * 点位主键ID
     */
    private Long id;

    /**
     * 绑定预警专题ID（可为空）
     */
    private Long alertId;

    /**
     * 绑定新闻ID（非空）
     */
    private Long newsId;

    /**
     * 经度 decimal(12,7)
     */
    private BigDecimal longitude;

    /**
     * 纬度 decimal(12,7)
     */
    private BigDecimal latitude;

    /**
     * 预警日期
     */
    private LocalDateTime alertDate;
}