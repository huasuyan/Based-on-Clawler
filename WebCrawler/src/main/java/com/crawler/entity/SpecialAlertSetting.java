package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

/**
 * 舆情预警专题实体
 * 对应数据库表 crawler_cron
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpecialAlertSetting {
    private Integer alertId;      // 专题ID（主键）
    private Long userId;            // 用户ID（外键）
    private String alertName;     // 专题名称
    private Integer triggerState;   // 启用状态，0：已停止，1：已启用
    private String targetSource;    // 数据源，如 xinhuanet
    private String keyWord;         // 预警词组，JSON格式，{"keywordGroups":[["关键词A1","关键词A2"],["关键词B1"]]}
    private String params;          // 可变参数，JSON格式，默认为null，{"searchFields":1,"sortField":0}
    private Integer frequency;      // 预警频率，0：实时，1：定时，2：定量
    private Integer alertTrigger;   // 预警消息定量触发
    private String timeRange;       // 预警时间范围，JSON格式
    private Integer alertMethod;    // 预警方式，0：短信，1：邮件，2：微信
    private Integer dedupEnable;    // 预警重复信息去重，0：不去重，1：去重
    private Integer state;          // 运行状态，-1：监测失败，等待下一次执行，0：等待下一次执行，1：爬取数据中，2：数据清洗中，3：数据保存中
    private Date createTime;        // 创建时间
    private Integer pendingCount;   // 累计待预警舆情数
    private Date lastTriggerTime;   // 上次触发时间
    private Date latestNewsTime;    // 最新舆情时间
    private Integer alertLevel;     // 预警等级
}
