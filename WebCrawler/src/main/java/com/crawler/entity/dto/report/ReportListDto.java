package com.crawler.entity.dto.report;

import com.crawler.entity.ReportResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportListDto {
    private Long reportId;
    private String reportName;
    private String reportType;
    private String monitorKeywords;
    private Date createTime;

    public ReportListDto(ReportResult c) {
        this.reportId = c.getReportId();
        this.reportName = c.getReportName();
        this.reportType = c.getReportType();
        this.monitorKeywords = c.getMonitorKeywords();
        this.createTime = c.getCreateTime();
    }

}
