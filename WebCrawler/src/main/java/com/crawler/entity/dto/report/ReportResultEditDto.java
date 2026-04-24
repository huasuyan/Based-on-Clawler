package com.crawler.entity.dto.report;

import lombok.Data;

@Data
public class ReportResultEditDto {
    private Long reportId;
    private String reportName;
    private String briefSummary;
    private String monitorSummary;
    private String opinionTrend;
    private String sourceMediaAnalysis;
    private String emotionAnalysis;
    private String regionDistribution;
    private String hotAnalysisWords;
    private String hotInformation;
    private String disposalOpinions;
}
