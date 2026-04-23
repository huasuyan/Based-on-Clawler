package com.crawler.entity;

import lombok.Data;

import java.util.Date;

/**
 * 舆情报告专题设置实体
 * 对应数据库表 special_report_setting
 */
@Data
public class SpecialReportSetting {
    private Long specialReportId;       // 舆情报告专题ID
    private String reportName;          // 报告专题名称
    private Long createUserId;          // 创建人ID
    private Object monitorKeywords;     // 监测词组 JSON {"keywordGroups":[["词A"]]}
    private String dataSource;          // 内容来源平台
    private Object params;              // 可变参数 JSON
    private String monitorRegion;       // 监测地域
    private Integer reportType;         // 报告类型：1即时报告 2定时报告
    private Object typeParams;          // 类型参数 JSON
    private Integer statusEnabled;      // 启用状态：1启用 0停用
    private Date createTime;            // 创建时间
    private Integer executeStatus;      // 执行状态：0等待执行 1爬取数据中 2生成报告中 3已完成
    private Date lastExecuteTime;       // 上一次报告生成执行时间
    private Date lastUpdateTime;            // 更新时间
}
