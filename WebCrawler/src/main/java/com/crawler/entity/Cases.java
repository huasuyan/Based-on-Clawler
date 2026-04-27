package com.crawler.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 办件处理实体
 * 对应数据库表 case_management
 */
@Data
public class Cases {
    private Long caseId;           // 办件唯一ID
    private String caseName;       // 办件名称
    private String caseInfo;       // 办件描述
    private Integer caseLevel;     // 办件等级：0紧急 1高 2中 3一般
    private BigDecimal money;      // 涉案金额
    private Long newsId;           // 对应的预警记录ID
    private Integer triggerState;  // 办件是否关闭：0已关闭 1已启用
    private Integer state;         // 办件状态：0待办 1办理中 2已归档 3异常
    private Date createTime;       // 创建时间
    private Date updateTime;       // 更新时间
}
