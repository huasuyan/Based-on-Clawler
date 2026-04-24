package com.crawler.entity.dto.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardTrendDto {
    private List<String> dates;
    private List<Long> newsCounts;
    private List<Integer> alertCounts;
}