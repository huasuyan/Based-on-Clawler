package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据清洗实体
 * 对应数据库表 clear_data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClearData {
    private Long clearId;           // 清洗数据ID（主键）
    private Long newsId;            // 对应新闻ID（外键关联 news_data.news_id）
    private Integer sensitivityLevel;//敏感度等级
    private String sensitivityLabel; // 敏感标签分类（可为null）
    private Integer sentimentType;  // 情绪极性（-1 = 负面， 0 = 中性， 1 = 正面）
}