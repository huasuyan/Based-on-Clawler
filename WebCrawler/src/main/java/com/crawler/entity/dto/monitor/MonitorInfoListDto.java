package com.crawler.entity.dto.monitor;

import lombok.Data;

/**
 * 检索舆情报告信息请求DTO
 * 接收前端传入的reportId、分页参数
 */
@Data
public class MonitorInfoListDto {
    private Long reportId;      // 专题ID（对应 alert_id 或 special_report_id）
    private Integer pageNum;    // 页码
    private Integer pageSize;   // 每页条数

    /**
     * 供 MyBatis XML 使用，自动计算 OFFSET
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
