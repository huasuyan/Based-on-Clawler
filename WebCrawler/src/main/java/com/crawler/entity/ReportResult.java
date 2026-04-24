package com.crawler.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReportResult {
    private Long reportId;
    private Long specialReportId;
    private String reportName;
    private String monitorKeywords;
    private String reportType;
    private String briefSummary;
    private String monitorSummary;
    private String opinionTrend;
    private String sourceMediaAnalysis;
    private String emotionAnalysis;
    private String regionDistribution;
    private String hotAnalysisWords;
    private String hotInformation;
    private String disposalOpinions;
    private Date createTime;
    private Date updateTime;
}
