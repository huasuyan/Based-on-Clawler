package com.crawler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.ReportResult;
import com.crawler.entity.dto.report.ReportListDto;
import com.crawler.entity.dto.report.ReportResultDto;
import com.crawler.entity.dto.report.ReportResultEditDto;
import com.crawler.entity.dto.report.ReportResultPageQueryDto;
import com.crawler.mapper.ReportResultMapper;
import com.crawler.service.ReportResultService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportResultServiceImpl implements ReportResultService {

    @Resource
    private ReportResultMapper reportResultMapper;

    @Override
    public Map<String, Object> pageList(ReportResultPageQueryDto queryDto) {
        List<ReportResult> list = reportResultMapper.pageList(queryDto);

        if (queryDto.getMonitorKeywords() != null) {
            JSONObject filterKw = JSONUtil.parseObj(queryDto.getMonitorKeywords().toString());
            JSONArray keywordGroups = filterKw.getJSONArray("keywordGroups");
            if (keywordGroups != null && !keywordGroups.isEmpty()) {
                List<String> filterWords = new ArrayList<>();
                for (int i = 0; i < keywordGroups.size(); i++) {
                    JSONArray group = keywordGroups.getJSONArray(i);
                    for (int j = 0; j < group.size(); j++) {
                        filterWords.add(group.getStr(j).toLowerCase());
                    }
                }
                if (!filterWords.isEmpty()) {
                    list = list.stream()
                            .filter(r -> {
                                if (StringUtils.isBlank(r.getMonitorKeywords())) {
                                    return false;
                                }
                                String kw = r.getMonitorKeywords().toLowerCase();
                                return filterWords.stream().anyMatch(kw::contains);
                            })
                            .collect(Collectors.toList());
                }
            }
        }

        List<ReportListDto> dtoList = list.stream()
                .map(ReportListDto::new)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        int total = reportResultMapper.countPageList(queryDto);
        result.put("total", total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("resultList", dtoList);
        return result;
    }

    @Override
    public Map<String, Object> detail(Long reportId) {
        ReportResult reportResult = reportResultMapper.selectById(reportId);
        if (reportResult == null) {
            throw new RuntimeException("舆情报告不存在");
        }
        ReportResultDto dto = new ReportResultDto(reportResult);
        Map<String, Object> result = new HashMap<>();
        result.put("reportResult", dto);
        return result;
    }

    @Override
    public void edit(ReportResultEditDto editDto) {
        if (editDto.getReportId() == null) {
            throw new RuntimeException("报告ID不能为空");
        }
        ReportResult existing = reportResultMapper.selectById(editDto.getReportId());
        if (existing == null) {
            throw new RuntimeException("舆情报告不存在");
        }
        reportResultMapper.update(editDto);
    }

    @Override
    public void delete(Long reportId) {
        ReportResult existing = reportResultMapper.selectById(reportId);
        if (existing == null) {
            throw new RuntimeException("舆情报告不存在");
        }
        reportResultMapper.deleteById(reportId);
    }
}
