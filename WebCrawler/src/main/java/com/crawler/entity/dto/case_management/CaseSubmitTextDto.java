package com.crawler.entity.dto.case_management;

import lombok.Data;

/**
 * 提交意见 请求DTO
 */
@Data
public class CaseSubmitTextDto {
    private Long caseId;           // 办件ID
    private Integer type;          // 意见类型：0办理意见 1归档意见 2启用说明 3停用说明 4异常说明
    private String content;        // 意见内容
}
