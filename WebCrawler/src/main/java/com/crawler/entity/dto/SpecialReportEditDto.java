package com.crawler.entity.dto;

import lombok.Data;

/**
 * 编辑舆情报告专题请求DTO
 * 注意：reportType 和 typeParams 不允许编辑，需先停用再删除重建
 */
@Data
public class SpecialReportEditDto {
    private Long specialReportId;       // 专题ID（必填）
    private String reportName;          // 报告专题名称
    private String monitorKeywords;     // 监测词组 JSON
    private String dataSource;          // 内容来源平台
    private String params;              // 其他参数（可选）
    private String monitorRegion;       // 监测地域
    private Integer statusEnabled;      // 状态：1启用 0停用
}
