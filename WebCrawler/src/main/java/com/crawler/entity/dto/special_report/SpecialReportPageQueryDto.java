package com.crawler.entity.dto.special_report;

import lombok.Data;

/**
 * 舆情报告专题分页查询DTO
 */
@Data
public class SpecialReportPageQueryDto {
    private Long createUserId;          // 从token获取，不由前端传入
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    // 可选筛选条件
    private String reportName;          // 报告名称（模糊查询）
    private Integer reportType;         // 报告类型：1即时 2定时
    private Integer statusEnabled;      // 启用状态：1启用 0停用
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
