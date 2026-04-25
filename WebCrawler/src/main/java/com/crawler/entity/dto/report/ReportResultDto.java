package com.crawler.entity.dto.report;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.ReportResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
public class ReportResultDto {
    private Long reportId;
    private Long specialReportId;
    private String reportName;
    private Object monitorKeywords;
    private String reportType;
    private String briefSummary;
    private String monitorSummary;
    private String opinionTrend;
    private String sourceMediaAnalysis;
    private String emotionAnalysis;
    private String regionDistributionList;
    private String regionDistribution;
    private String hotAnalysisWordsList;
    private String hotInformationList;
    private String disposalOpinions;
    private Date createTime;
    private Date updateTime;

    public ReportResultDto(ReportResult r) {
        this.reportId = r.getReportId();
        this.specialReportId = r.getSpecialReportId();
        this.reportName = r.getReportName();
        this.monitorKeywords = parseJson(r.getMonitorKeywords());
        this.reportType = r.getReportType();
        this.briefSummary = r.getBriefSummary();
        this.monitorSummary = r.getMonitorSummary();
        this.opinionTrend = r.getOpinionTrend();
        this.sourceMediaAnalysis = r.getSourceMediaAnalysis();
        this.emotionAnalysis = r.getEmotionAnalysis();
        this.regionDistribution = r.getRegionDistribution();
        this.regionDistributionList = r.getRegionDistributionList();
        this.hotAnalysisWordsList = r.getHotAnalysisWordsList();
        this.hotInformationList = r.getHotInformationList();
        this.disposalOpinions = r.getDisposalOpinions();
        this.createTime = r.getCreateTime();
        this.updateTime = r.getUpdateTime();
    }

    private static Object parseJson(Object raw) {
        if (raw == null) return null;
        String json = raw.toString().trim();
        if (StringUtils.isBlank(json)) return null;
        if (json.startsWith("{")) return JSONUtil.parseObj(json);
        if (json.startsWith("[")) return JSONUtil.parseArray(json);
        return raw;
    }
}
