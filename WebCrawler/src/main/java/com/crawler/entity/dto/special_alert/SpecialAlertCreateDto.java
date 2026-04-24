package com.crawler.entity.dto.special_alert;

import lombok.Data;

/**
 * 新增预警专题请求DTO
 * 对应接口：POST /api/v1/specialAlert/create
 */
@Data
public class SpecialAlertCreateDto {
    // userId 由 token 从 request 中获取，不由前端传入
    private Long userId;
    private String alertName;     // 专题名称
    private String keyWord;         // 预警词组，JSON格式，{"keywordGroups":[["关键词A1","关键词A2"],["关键词B1"]]}
    private String targetSource;    // 数据源
    private String params;          // 可变参数
    private Integer dedupEnable;    // 去重：0不去重 1去重
    private Integer frequency;      // 预警频率，0：实时，1：定时，2：定量
    private Integer alertTrigger;   // 定量触发阈值（frequency=2时必填）
    private String timeRange;       // 预警时间范围，JSON格式
    private Integer alertMethod;    // 预警方式，0：全部、1：站内信、2：邮箱
    private Integer alertLevel;     // 预警等级，1：一级、2：二级、3：三级
}
