package com.crawler.entity.dto.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportResultPageQueryDto {
    private List<Long> specialReportIdList;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String reportType;
    private String reportName;
    private Object monitorKeywords;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
