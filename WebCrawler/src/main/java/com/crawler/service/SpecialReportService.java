package com.crawler.service;

import com.crawler.entity.dto.SpecialReportCreateDto;
import com.crawler.entity.dto.SpecialReportEditDto;
import com.crawler.entity.dto.SpecialReportPageQueryDto;

import java.util.Map;

public interface SpecialReportService {

    // 新增报告专题
    Map<String, Object> create(SpecialReportCreateDto createDto);

    // 编辑报告专题
    void edit(SpecialReportEditDto editDto);

    // 分页列表查询
    Map<String, Object> pageList(SpecialReportPageQueryDto queryDto);

    // 删除报告专题（须处于停用状态）
    void delete(Long specialReportId);
}
