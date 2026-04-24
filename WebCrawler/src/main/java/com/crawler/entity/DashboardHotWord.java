package com.crawler.entity;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class DashboardHotWord {
    private Long id;
    private LocalDate statDate;
    private String statType;
    private String word;
    private Integer wordCount;
    private String wordType;
    private Date createTime;
}