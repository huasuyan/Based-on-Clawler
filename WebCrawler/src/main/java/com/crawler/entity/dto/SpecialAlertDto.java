package com.crawler.entity.dto;

import com.crawler.entity.SpecialAlertSetting;
import com.crawler.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialAlertDto {
    private Integer alertId;      // 专题ID（主键）
    private Long userId;            // 用户ID（外键）
    private String alertName;     // 专题名称
    private Integer triggerState;   // 启用状态，0：已停止，1：已启用
    private String targetSource;    // 数据源，如 xinhuanet
    private Object keyWord;         // 预警词组，JSON格式，{"keywordGroups":[["关键词A1","关键词A2"],["关键词B1"]]}
    private Object params;          // 可变参数，JSON格式，默认为null，{"searchFields":1,"sortField":0}
    private Integer frequency;      // 预警频率，0：实时，1：定时，2：定量
    private Integer alertTrigger;   // 预警消息定量触发
    private Object timeRange;       // 预警时间范围，JSON格式
    private Integer alertMethod;    // 预警方式，0：短信，1：邮件，2：微信
    private Integer dedupEnable;    // 预警重复信息去重，0：不去重，1：去重
    private Integer state;          // 运行状态，-1：监测失败，等待下一次执行，0：等待下一次执行，1：爬取数据中，2：数据清洗中，3：数据保存中
    private Date createTime;        // 创建时间
    private Integer pendingCount;   // 累计待预警舆情数
    private Date lastTriggerTime;   // 上次触发时间
    private Date latestNewsTime;    // 最新舆情时间
    private Integer alertLevel;     // 预警等级
    private String userName;         // 用户名

    /**
     * 从实体构建DTO。
     * 三个 JSON 字符串字段通过方法转换为结构化对象
     * 若原始值为 null 或空字符串则保持 null，不抛出异常。
     */
    public SpecialAlertDto(SpecialAlertSetting c) {
        this.alertId = c.getAlertId();
        this.userId = c.getUserId();
        this.alertName = c.getAlertName();
        this.triggerState = c.getTriggerState();
        this.targetSource = c.getTargetSource();
        this.keyWord      = CommonUtil.parseJson(c.getKeyWord());
        this.params       = CommonUtil.parseJson(c.getParams());
        this.frequency    = c.getFrequency();
        this.alertTrigger = c.getAlertTrigger();
        this.timeRange    = CommonUtil.parseJson(c.getTimeRange());
        this.alertMethod  = c.getAlertMethod();
        this.state        = c.getState();
        this.pendingCount = c.getPendingCount();
        this.lastTriggerTime = c.getLastTriggerTime();
        this.latestNewsTime = c.getLatestNewsTime();
        this.alertLevel    = c.getAlertLevel();
    }
}
