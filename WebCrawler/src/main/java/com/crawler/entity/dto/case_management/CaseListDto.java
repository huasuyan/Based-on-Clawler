package com.crawler.entity.dto.case_management;

import com.crawler.entity.Cases;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 办件列表项 响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseListDto {
    private Long caseId;           // 办件ID
    private String caseName;       // 办件名称
    private String caseInfo;       // 办件描述
    private Integer caseLevel;     // 办件等级
    private BigDecimal money;      // 涉案金额
    private Long newsId;           // 预警记录ID
    private Integer triggerState;  // 是否关闭：0已关闭 1已启用
    private Integer state;         // 办件状态：0待办 1办理中 2已归档 3异常
    private Long userId;           // 用户ID
    private Long deptId;           // 部门ID
    private String deptName;       // 部门名称（联表查询填充）
    private Date createTime;       // 创建时间
    private Date updateTime;       // 更新时间

    public CaseListDto(Cases c) {
        this.caseId = c.getCaseId();
        this.caseName = c.getCaseName();
        this.caseInfo = c.getCaseInfo();
        this.caseLevel = c.getCaseLevel();
        this.money = c.getMoney();
        this.newsId = c.getNewsId();
        this.triggerState = c.getTriggerState();
        this.state = c.getState();
        this.userId = c.getUserId();
        this.deptId = c.getDeptId();
        this.deptName = c.getDeptName();
        this.createTime = c.getCreateTime();
        this.updateTime = c.getUpdateTime();
    }
}
