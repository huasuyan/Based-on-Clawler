package com.crawler.service;

import com.crawler.entity.dto.report.ReportResultEditDto;
import com.crawler.entity.dto.report.ReportResultPageQueryDto;

import java.util.Map;

public interface ReportResultService {

    Map<String, Object> pageList(ReportResultPageQueryDto queryDto);

    Map<String, Object> detail(Long reportId);

    void edit(ReportResultEditDto editDto);

    void delete(Long reportId);
}
