package com.crawler.entity.dto.dashboard;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardSummaryDto {
    private Long totalNews;
    private Long totalNewsArticle;
    private Long totalNewsVideo;
    private Long totalAlert;
    private Long totalAlertArticle;
    private Long totalAlertVideo;
    private Long totalCase;
    private Integer caseInProgress;
    private Integer caseFinished;
    private BigDecimal caseConversionRate;
    private Long todayNews;
    private Long todayNewsArticle;
    private Long todayNewsVideo;
    private BigDecimal todayNewsYoy;
    private Long todayAlert;
    private BigDecimal todayAlertYoy;
    private Integer todayAlertLevel1;
    private Integer todayAlertLevel2;
    private Integer todayAlertLevel3;
}