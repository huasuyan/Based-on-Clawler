package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.dto.alertMap.AlertMapDto;
import com.crawler.service.AlertMapCoordinateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertMap")
public class AlertMapController {

    @Resource
    private AlertMapCoordinateService alertMapCoordinateService;

    // 获取所有预警坐标
    @GetMapping("/list")
    public Result list() {
        List<AlertMapDto> alertMapCoordinateList = alertMapCoordinateService.getAll();
        return Result.success(alertMapCoordinateList);
    }

    // 根据预警ID获取
    @GetMapping("/alert")
    public Result getByAlertId(@RequestParam Long alertId) {
        List<AlertMapDto> alertMapCoordinateList = alertMapCoordinateService.getByAlertId(alertId);
        return Result.success(alertMapCoordinateList);
    }

    // 获取今日预警
    @GetMapping("/today")
    public Result today(@RequestParam String dayType) {
        List<AlertMapDto> alertMapCoordinateList = alertMapCoordinateService.getAlertMap(dayType);
        return Result.success(alertMapCoordinateList);
    }
}