package com.crawler.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class DashboardPlatformStats {
    private Long id;
    private LocalDate statDate;
    private String statType;
    private String platform;
    private Integer sensitiveCount;
    private Integer neutralCount;
    private Integer normalCount;
    private Integer totalCount;
    private Date createTime;
}