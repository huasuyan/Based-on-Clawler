package com.crawler.entity.dto.monitor;

import lombok.Data;

import java.util.List;

/**
 * 舆情监测文章查询请求DTO
 * 接收前端筛选条件：时间范围、敏感等级、来源、地域、关键词
 */
@Data
public class MonitorArticleQueryDto {
    private String startTime;               // 开始时间，格式：yyyy-MM-dd HH:mm:ss
    private String endTime;                 // 截止时间，格式：yyyy-MM-dd HH:mm:ss
    private List<Integer> sensitivityLevel; // 敏感等级选中状态 [普通,低敏感,中敏感,高敏感]，1选中 0未选中
    private String source;                  // 文章来源
    private String region;                  // 地域
    private String keyWord;                 // 关键词搜索
}
