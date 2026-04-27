package com.crawler.controller;

import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.dto.report.ReportResultEditDto;
import com.crawler.entity.dto.report.ReportResultPageQueryDto;
import com.crawler.service.ReportResultService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/reportResult")
public class ReportResultController {

    @Resource
    private ReportResultService reportResultService;

    @PostMapping("/pageList")
    @RequirePermission(module = "report", action = "report_select")
    public Result pageList(@RequestBody ReportResultPageQueryDto queryDto) {
        // TODO 用户访问权限内所有报告都能访问
        Map<String, Object> data = reportResultService.pageList(queryDto);
        return Result.success(data);
    }

    @GetMapping("/detail")
    @RequirePermission(module = "report", action = "report_select")
    public Result detail(@RequestParam Long reportId) {
        Map<String, Object> data = reportResultService.detail(reportId);
        return Result.success(data);
    }

    @PostMapping("/edit")
    @RequirePermission(module = "report", action = "report_update")
    public Result edit(@RequestBody ReportResultEditDto editDto) {
        reportResultService.edit(editDto);
        return Result.success();
    }

    @GetMapping("/delete")
    @RequirePermission(module = "report", action = "report_delete")
    public Result delete(@RequestParam Long reportId) {
        reportResultService.delete(reportId);
        return Result.success();
    }
}
