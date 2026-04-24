package com.crawler.service;

import com.crawler.entity.dto.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    DashboardSummaryDto getSummary();
    DashboardTrendDto getTrend(int days);
    DashboardPlatformDto getPlatformStats(String statType, LocalDate startDate, LocalDate endDate);
    List<RegionRankDto> getRegionStats(String statType, LocalDate startDate, LocalDate endDate);
    List<HotWordDto> getHotWords(String statType, String wordType, LocalDate startDate, LocalDate endDate);
    void refreshStats();
}