package com.crawler.service.impl;

import com.crawler.entity.dto.ClearNewsData;
import com.crawler.entity.dto.monitor.MonitorArticleQueryDto;
import com.crawler.entity.dto.monitor.MonitorInfoListDto;

import com.crawler.mapper.MonitorMapper;
import com.crawler.service.MonitorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // ---- Step 4: 组装返回结果 ----
        Map<String, Object> result = new HashMap<>();
        result.put("dataList", clearNewsDataList);
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

        // 组装返回结果，格式与 /specialAlert/infoList 一致
        Map<String, Object> result = new HashMap<>();
        result.put("datalist", clearNewsDataList);
        result.put("total", total);
        result.put("pageNum", queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        return result;
    }

    // 返回所有新闻来源名称，供前端下拉选择
    @Override
    public List<String> searchAllSource() {
        return monitorMapper.selectAllSources();
    }
}
