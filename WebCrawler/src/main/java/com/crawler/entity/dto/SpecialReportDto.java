package com.crawler.entity.dto;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialReportSetting;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
        this.monitorKeywords  = parseJson(s.getMonitorKeywords());
        this.dataSource       = s.getDataSource();
        this.params           = parseJson(s.getParams());
        this.monitorRegion    = s.getMonitorRegion();
        this.reportType       = s.getReportType();
        this.typeParams       = parseJson(s.getTypeParams());
        this.statusEnabled    = s.getStatusEnabled();
        this.createTime       = s.getCreateTime();
        this.executeStatus    = s.getExecuteStatus();
        this.lastExecuteTime  = s.getLastExecuteTime();
        this.lastUpdateTime     = s.getLastUpdateTime();
    }

    private static Object parseJson(Object raw) {
        if (raw == null) return null;
        String json = raw.toString().trim();
        if (StringUtils.isBlank(json)) return null;
        if (json.startsWith("{")) return JSONUtil.parseObj(json);
        if (json.startsWith("[")) return JSONUtil.parseArray(json);
        return raw;
    }
}
