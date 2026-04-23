package com.crawler.entity.dto;

import lombok.Data;

/**
 * 新增舆情报告专题请求DTO
 */
@Data
public class SpecialReportCreateDto {
    // userId 从 token 解析，不由前端传入
    private Long createUserId;
    private String reportName;          // 报告专题名称（必填）
    private String monitorKeywords;     // 监测词组
    private String dataSource;          // 内容来源平台（可选）
    private String params;              // 其他参数（可选）
    private String monitorRegion;       // 监测地域（可选）
    private Integer statusEnabled;      // 状态：1启用 0停用（可选，默认1）
    private Integer reportType;         // 报告类型：1即时 2定时（必填）
    /**
     * 类型参数（必填）
     *
     * 即时报告(reportType=1)：
     * {"start_date":"2026-04-20","end_date":"2026-04-30"}
     *
     * 定时报告(reportType=2) 按天：
     * {"cycle":"daily","time":"08:00"}
     *
     * 定时报告(reportType=2) 按周：
     * {"cycle":"weekly","weekday":1,"time":"08:00"}
     *
     * 定时报告(reportType=2) 按月：
     * {"cycle":"monthly","day":7,"time":"08:00"}
     */
    private String typeParams;
}
