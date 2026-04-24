package com.crawler.controller;

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
    public Result pageList(@RequestBody ReportResultPageQueryDto queryDto) {
        Map<String, Object> data = reportResultService.pageList(queryDto);
        return Result.success(data);
    }

    @GetMapping("/detail")
    public Result detail(@RequestParam Long reportId) {
        Map<String, Object> data = reportResultService.detail(reportId);
        return Result.success(data);
    }

    @PostMapping("/edit")
    public Result edit(@RequestBody ReportResultEditDto editDto) {
        reportResultService.edit(editDto);
        return Result.success();
    }

    @GetMapping("/delete")
        public Result delete(@RequestParam Long reportId) {
        reportResultService.delete(reportId);
        return Result.success();
    }
}
