package com.crawler.entity.dto.case_management;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 预警记录转为办件 请求DTO
 */
@Data
public class CaseCreateDto {
    private String caseName;       // 办件名称
    private String caseInfo;       // 办件描述
    private Integer caseLevel;     // 办件等级
    private BigDecimal money;      // 涉案金额
    private Long newsId;           // 预警记录ID
}
