package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.dto.dashboard.*;
import com.crawler.service.DashboardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取顶部统计卡片数据
     * GET /dashboard/summary
     */
    @GetMapping("/summary")
    public Result summary() {
        DashboardSummaryDto data = dashboardService.getSummary();
        return Result.success(data);
    }

    /**
     * 获取趋势图数据（折线/柱状）
     * GET /dashboard/trend?days=7
     */
    @GetMapping("/trend")
    public Result trend(@RequestParam(defaultValue = "7") int days) {
        if (days > 30) days = 30;
        DashboardTrendDto data = dashboardService.getTrend(days);
        return Result.success(data);
    }

    /**
     * 获取平台分布数据
     * GET /dashboard/platform?statType=weekly&startDate=2024-04-01&endDate=2024-04-07
     */
    @PostMapping("/platform")
    public Result platform(@RequestBody DashboardCommonDto dto) {
        DashboardPlatformDto data =
                dashboardService.getPlatformStats(dto.getStatType(), dto.getStartDate(), dto.getEndDate());
        return Result.success(data);
    }

    /**
     * 获取地域分布数据
     * GET /dashboard/region?statType=weekly
     */
    @PostMapping("/region")
    public Result region(@RequestBody DashboardCommonDto dto) {
        List<RegionRankDto> data =
                dashboardService.getRegionStats(dto.getStatType(), dto.getStartDate(), dto.getEndDate());
        return Result.success(data);
    }

    /**
     * 获取热词/活跃用户词云
     * POST /dashboard/hotWords
     */
    @PostMapping("/hotWords")
    public Result hotWords(@RequestBody DashboardCommonDto dto) {
        List<HotWordDto> data =
                dashboardService.getHotWords(dto.getStatType(), dto.getWordType(),
                        dto.getStartDate(), dto.getEndDate());
        return Result.success(data);
    }

    /**
     * 手动刷新大屏统计数据
     * POST /dashboard/refresh
     */
    @PostMapping("/refresh")
    public Result refresh() {
        dashboardService.refreshStats();
        return Result.success("大屏数据刷新成功");
    }
}