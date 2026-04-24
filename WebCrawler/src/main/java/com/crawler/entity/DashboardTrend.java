package com.crawler.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class DashboardTrend {
    private Long id;
    private LocalDate statDate;
    private Long newsCount;
    private Integer alertCount;
    private Date createTime;
}