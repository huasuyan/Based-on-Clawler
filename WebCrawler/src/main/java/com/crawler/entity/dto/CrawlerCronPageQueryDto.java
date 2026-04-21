package com.crawler.entity.dto;

import lombok.Data;

/**
 * 预警专题分页查询DTO
 * 对应接口：POST /api/v1/crawlerCron/pageList
 *          POST /api/v1/crawlerCron/searchBycrawlerInfo
 */
@Data
public class CrawlerCronPageQueryDto {
    // userId 由 token 从 request 中获取
    private Long userId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    // 以下为可选筛选条件（searchBycrawlerInfo接口使用）
    private String crawlerName;     // 专题名称（模糊查询）
    private String keyWord;         // 预警词（模糊查询）
    private Integer triggerState;   // 启用状态：0停止 1启用
    private String targetSource;    // 数据源
    /**
     * 供 MyBatis XML 使用，自动计算 OFFSET
     * XML中写: limit #{pageSize} offset #{offset}
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
