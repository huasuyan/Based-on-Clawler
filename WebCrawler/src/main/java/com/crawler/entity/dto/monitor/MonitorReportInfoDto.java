package com.crawler.entity.dto.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预警/专题列表响应DTO
 * 用于 /searchAllReport 接口返回预警专题和报告专题的列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorReportInfoDto {
    private Long reportId;      // 专题ID（对应 alert_id 或 special_report_id）
    private String reportName;  // 专题名称
}
