package com.crawler.entity.dto.special_alert;

import lombok.Data;

/**
 * 编辑预警专题请求DTO
 * 对应接口：POST /api/v1/crawlerCron/edit
 * 注意：编辑要求专题处于关闭状态（triggerState=0）
 */
@Data
public class SpecialAlertEditDto {
    private Long alertId;      // 专题ID（必填）
    private String alertName;     // 专题名称
    private String keyWord;         // 预警词组，JSON格式，{"keywordGroups":[["关键词A1","关键词A2"],["关键词B1"]]}
    private String targetSource;    // 数据源
    private String params;          // 可变参数
    private Integer dedupEnable;    // 去重
    private Integer frequency;      // 预警频率
    private Integer alertTrigger;   // 定量触发
    private String timeRange;       // 预警时间范围，JSON格式
    private Integer alertMethod;    // 预警方式
    private Integer alertLevel;     // 预警等级
}
