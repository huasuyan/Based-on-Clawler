package com.crawler.entity.dto.case_management;

import lombok.Data;

import java.util.List;

/**
 * 条件查询办件列表 请求DTO
 */
@Data
public class CasePageQueryDto {
    private Integer pageNum;           // 页码
    private Integer pageSize;          // 每页条数
    private String caseName;           // 办件名称（可选，模糊查询）
    private Integer state;             // 办件状态（可选）
    private Integer caseLevel;         // 办件等级（可选）
    private Integer triggerState;      // 是否关闭（可选）
    private String createTimeStart;    // 创建时间-开始
    private String createTimeEnd;      // 创建时间-结束
    private String updateTimeStart;    // 更新时间-开始
    private String updateTimeEnd;      // 更新时间-结束
    private List<Long> deptIdList;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
