package com.crawler.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class DashboardRegionStats {
    private Long id;
    private LocalDate statDate;
    private String statType;
    private String region;
    private Integer newsCount;
    private Date createTime;
}