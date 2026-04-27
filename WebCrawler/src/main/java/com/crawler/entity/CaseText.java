package com.crawler.entity;

import lombok.Data;
import java.util.Date;

/**
 * 办件用户上传信息实体
 * 对应数据库表 case_user_text
 */
@Data
public class CaseText {
    private Long textId;           // 信息表ID
    private Long caseId;           // 办件ID
    private Long userId;           // 用户ID
    private Integer type;          // 0办理意见 1归档意见 2启用说明 3停用说明 4异常说明
    private String content;        // 用户上传内容
    private Date createTime;       // 创建时间
}
