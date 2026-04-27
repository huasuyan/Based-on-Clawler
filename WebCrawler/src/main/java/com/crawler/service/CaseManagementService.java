package com.crawler.service;

import com.crawler.entity.dto.case_management.*;
import java.util.Map;

public interface CaseManagementService {

    // 预警记录转为办件
    Map<String, Object> create(CaseCreateDto createDto, Long userId);

    // 条件查询办件列表
    Map<String, Object> pageList(CasePageQueryDto queryDto);

    // 提交意见
    Map<String, Object> submitText(CaseSubmitTextDto submitDto, Long userId);

    // 办理办件（state -> 1）
    Map<String, Object> process(Long caseId);

    // 归档办件（state -> 2）
    Map<String, Object> archive(Long caseId);

    // 关闭办件（triggerState -> 0）
    Map<String, Object> close(Long caseId);

    // 启用办件（triggerState -> 1）
    Map<String, Object> enable(Long caseId);

    // 标记异常办件（state -> 3）
    Map<String, Object> markException(Long caseId);

    // 查询办件上传信息列表
    Map<String, Object> textList(Long caseId);
}
