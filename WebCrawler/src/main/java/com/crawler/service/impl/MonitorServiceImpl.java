package com.crawler.service.impl;

import com.crawler.entity.dto.ClearNewsData;
import com.crawler.entity.dto.monitor.MonitorArticleDto;
import com.crawler.entity.dto.monitor.MonitorArticleQueryDto;
import com.crawler.entity.dto.monitor.MonitorInfoListDto;
import com.crawler.entity.dto.monitor.MonitorReportInfoDto;
import com.crawler.mapper.MonitorMapper;
import com.crawler.service.MonitorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 舆情监测模块 Service 实现
 * 实现文章查询、统计图表、预警/专题展示等功能
 */
@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    @Resource
    private MonitorMapper monitorMapper;

    /**
     * 文章监测查询
     * 逻辑说明：
     * 1. 若关键词不为空，先模糊匹配 special_alert_setting 和 special_report_setting 获取关联ID
     * 2. 将前端传入的 sensitivityLevel 选中状态数组转换为实际敏感等级列表
     * 3. 组装多条件查询，关联 news_data 和 clear_data 表
     */
    @Override
    public Map<String, Object> queryArticle(MonitorArticleQueryDto queryDto) {
        // ---- Step 1: 处理关键词 ----
        // 若关键词不为空，分别查询预警专题和报告专题，获取匹配的ID列表
        List<Integer> alertIds = new ArrayList<>();
        List<Long> reportIds = new ArrayList<>();
        String keyWord = queryDto.getKeyWord();
        if (keyWord != null && !keyWord.trim().isEmpty()) {
            // 模糊匹配预警专题的名称或关键词，获取alert_id列表
            alertIds = monitorMapper.searchAlertIdsByKeyword(keyWord.trim());
            // 模糊匹配报告专题的名称或监测关键词，获取special_report_id列表
            reportIds = monitorMapper.searchReportIdsByKeyword(keyWord.trim());
        }

        // ---- Step 2: 处理敏感等级 ----
        // sensitivityLevel 数组：[0]=普通、[1]=低敏感、[2]=中敏感、[3]=高敏感
        // 值为 1 表示选中，0 表示未选中
        List<Integer> selectedLevels = new ArrayList<>();
        List<Integer> sensitivityLevel = queryDto.getSensitivityLevel();
        if (sensitivityLevel != null && !sensitivityLevel.isEmpty()) {
            for (int i = 0; i < sensitivityLevel.size(); i++) {
                if (sensitivityLevel.get(i) == 1) {
                    selectedLevels.add(i);
                }
            }
        }

        // ---- Step 3: 查询数据（不分页，返回所有匹配结果） ----
        List<ClearNewsData> clearNewsDataList = monitorMapper.queryArticleList(
                queryDto.getStartTime(),
                queryDto.getEndTime(),
                selectedLevels.isEmpty() ? null : selectedLevels,
                queryDto.getSource(),
                queryDto.getRegion(),
                alertIds.isEmpty() ? null : alertIds,
                reportIds.isEmpty() ? null : reportIds,
                0,            // 查询全部数据，offset从0开始
                Integer.MAX_VALUE  // 使用最大限制获取全部数据
        );

        // ---- Step 4: 转换为前端需要的格式 ----
        List<MonitorArticleDto> dataList = convertToArticleDto(clearNewsDataList);

        // ---- Step 5: 组装返回结果 ----
        Map<String, Object> result = new HashMap<>();
        result.put("dataList", dataList);
        return result;
    }

    /**
     * 生成统计图表
     * 接收前端已筛选的文章数据，由前端自行生成统计图表
     * 后端在此处可将数据存入缓存或日志，供后续分析使用
     */
    @Override
    public Map<String, Object> generateStatistics(List<Map<String, Object>> dataList) {
        // 目前统计图表的生成逻辑在前端完成，后端仅确认接收成功
        // 此处可扩展：将dataList存入Redis或数据库用于后续分析
        log.info("收到统计图表数据，共 {} 条记录", dataList != null ? dataList.size() : 0);
        return new HashMap<>();
    }

    /**
     * 展示预警/专题列表
     * 查询所有预警专题和报告专题，合并返回给前端
     */
    @Override
    public Map<String, Object> searchAllReport() {
        // ---- 查询所有预警专题 ----
        List<MonitorReportInfoDto> alertInfos = new ArrayList<>();
        List<com.crawler.entity.SpecialAlertSetting> alertList = monitorMapper.selectAllAlerts();
        if (alertList != null) {
            for (com.crawler.entity.SpecialAlertSetting alert : alertList) {
                alertInfos.add(new MonitorReportInfoDto(
                        Long.valueOf(alert.getAlertId()),
                        alert.getAlertName()
                ));
            }
        }

        // ---- 查询所有启用中的报告专题 ----
        List<MonitorReportInfoDto> reportInfos = new ArrayList<>();
        List<com.crawler.entity.SpecialReportSetting> reportList = monitorMapper.selectAllReports();
        if (reportList != null) {
            for (com.crawler.entity.SpecialReportSetting report : reportList) {
                reportInfos.add(new MonitorReportInfoDto(
                        report.getSpecialReportId(),
                        report.getReportName()
                ));
            }
        }

        // ---- 组装返回 ----
        Map<String, Object> result = new HashMap<>();
        result.put("alertInfos", alertInfos);
        result.put("reportInfos", reportInfos);
        return result;
    }

    /**
     * 检索舆情报告信息
     * 根据前端传入的reportId和分页参数，查询关联的文章数据
     * reportId可对应 alert_id 或 special_report_id
     */
    @Override
    public Map<String, Object> queryInfoList(MonitorInfoListDto queryDto) {
        if (queryDto.getReportId() == null) {
            throw new RuntimeException("专题ID不能为空");
        }

        // 查询数据
        List<ClearNewsData> clearNewsDataList = monitorMapper.queryInfoList(
                queryDto.getReportId(),
                queryDto.getOffset(),
                queryDto.getPageSize()
        );

        // 统计总数
        int total = monitorMapper.countInfoList(queryDto.getReportId());

        // 转换为前端所需格式
        List<MonitorArticleDto> dataList = convertToArticleDto(clearNewsDataList);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("dataList", dataList);
        result.put("total", total);
        result.put("pageNum", queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        return result;
    }

    /**
     * 将 ClearNewsData 列表转换为 MonitorArticleDto 列表
     * 适配前端需要的字段名和时间格式
     */
    private List<MonitorArticleDto> convertToArticleDto(List<ClearNewsData> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return sourceList.stream().map(item -> {
            MonitorArticleDto dto = new MonitorArticleDto();
            dto.setTitle(item.getTitle());
            dto.setContent(item.getContent());
            // 格式化发布时间为 yyyy-MM-dd
            if (item.getPublishTime() != null) {
                dto.setPublishTime(dateFormat.format(item.getPublishTime()));
            }
            dto.setSource(item.getSource());
            dto.setUrl(item.getOriginalUrl());
            // news_data表中暂无picUrl字段，前端可自行处理
            dto.setPicUrl("");
            return dto;
        }).collect(Collectors.toList());
    }
}
