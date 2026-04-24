package com.crawler.entity.dto.report;

import lombok.Data;

@Data
public class ReportResultPageQueryDto {
    private Long specialReportId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
        private String reportType;
    private String reportName;
    private Object monitorKeywords;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
