package com.crawler.entity.dto;

import com.crawler.entity.SpecialReportSetting;
import com.crawler.util.CommonUtil;
import lombok.Data;

import java.util.Date;

/**
 * 舆情报告专题返回DTO
 * JSON字段自动反序列化为结构化对象，前端直接收到嵌套对象
 */
@Data
public class SpecialReportDto {
    private Long specialReportId;
    private String reportName;
    private Long createUserId;
    private Object monitorKeywords;     // 反序列化后的JSON对象
    private String dataSource;
    private Object params;
    private String monitorRegion;
    private Integer reportType;
    private Object typeParams;          // 反序列化后的JSON对象
    private Integer statusEnabled;
    private Date createTime;
    private Integer executeStatus;
    private Date lastExecuteTime;
    private Date lastUpdateTime;            // 更新时间

    public SpecialReportDto(SpecialReportSetting s) {
        this.specialReportId  = s.getSpecialReportId();
        this.reportName       = s.getReportName();
        this.createUserId     = s.getCreateUserId();
        this.monitorKeywords  = CommonUtil.parseJson(s.getMonitorKeywords());
        this.dataSource       = s.getDataSource();
        this.params           = CommonUtil.parseJson(s.getParams());
        this.monitorRegion    = s.getMonitorRegion();
        this.reportType       = s.getReportType();
        this.typeParams       = CommonUtil.parseJson(s.getTypeParams());
        this.statusEnabled    = s.getStatusEnabled();
        this.createTime       = s.getCreateTime();
        this.executeStatus    = s.getExecuteStatus();
        this.lastExecuteTime  = s.getLastExecuteTime();
        this.lastUpdateTime     = s.getLastUpdateTime();
    }

}
