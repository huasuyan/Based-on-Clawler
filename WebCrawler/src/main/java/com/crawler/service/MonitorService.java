package com.crawler.service;

import com.crawler.entity.dto.monitor.MonitorArticleQueryDto;
import com.crawler.entity.dto.monitor.MonitorInfoListDto;

import java.util.List;
import java.util.Map;

/**
 * 舆情监测模块 Service 接口
 * 提供文章查询、统计图表、预警/专题列表检索等功能
 */
public interface MonitorService {

    // 文章监测查询：基于用户筛选条件获取舆情文章列表
    Map<String, Object> queryArticle(MonitorArticleQueryDto queryDto);

    // 生成统计图表：接收前端已筛选的文章数据，返回确认信息
    Map<String, Object> generateStatistics(List<Map<String, Object>> dataList);

    // 展示预警专题列表：返回用户可见的所有预警专题和报告专题
    Map<String, Object> searchAllReport();

    // 检索舆情报告信息：根据专题ID分页查询关联文章
    Map<String, Object> queryInfoList(MonitorInfoListDto queryDto);
}
