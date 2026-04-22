package com.crawler.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerNoneCreate {
    private Integer crawlerId;      // 任务ID
    private Long userId;         // 用户ID（从token解析）
    private String crawlerName;     // 任务名
    private Integer state;        // 状态
    private String targetSource;    // 数据源
    private String keyWord;         // 关键词
    private String params;          // 可变参数
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime finishTime; // 完成时间
}
