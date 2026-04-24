package com.crawler.entity.dto.dashboard;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardCommonDto {
    private String statType;
    private String wordType;
    private LocalDate startDate;
    private LocalDate endDate;
}
