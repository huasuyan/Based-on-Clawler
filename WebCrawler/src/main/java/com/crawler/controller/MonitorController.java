package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.dto.monitor.MonitorArticleQueryDto;
import com.crawler.entity.dto.monitor.MonitorInfoListDto;
import com.crawler.service.MonitorService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 舆情监测 Controller
 * 提供文章监测查询、统计图表生成、预警/专题列表展示、报告信息检索等接口
 * 基础地址：/api/v1/monitor
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    /**
     * 文章监测查询
     * 根据时间范围、敏感等级、文章来源、地域、关键词等条件筛选舆情文章
     * 对于关键词，先模糊匹配预警/报告专题获取关联ID，再查询news_data表
     */
    @PostMapping("/article")
    public Result queryArticle(HttpServletRequest request,
                               @RequestBody MonitorArticleQueryDto queryDto) {
        Map<String, Object> data = monitorService.queryArticle(queryDto);
        return Result.success(data);
    }

    /**
     * 生成统计图表
     * 接收前端已筛选的文章数据，前端自行完成图表渲染
     */
    @PostMapping("/statistics")
    public Result generateStatistics(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) body.get("dataList");
        Map<String, Object> data = monitorService.generateStatistics(dataList);
        return Result.success(data);
    }

    /**
     * 展示预警/专题列表
     * 返回所有预警专题和报告专题，供前端下拉选择
     */
    @GetMapping("/searchAllReport")
    public Result searchAllReport() {
        Map<String, Object> data = monitorService.searchAllReport();
        return Result.success(data);
    }

    /**
     * 检索舆情报告信息
     * 根据专题ID和分页参数，查询该专题关联的所有文章
     */
    @PostMapping("/infoList")
    public Result queryInfoList(@RequestBody MonitorInfoListDto queryDto) {
        Map<String, Object> data = monitorService.queryInfoList(queryDto);
        return Result.success(data);
    }
}
