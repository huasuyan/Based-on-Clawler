package com.crawler.entity;

import lombok.Data;
import java.util.Date;

@Data
public class AlertMessage {
    private Long    messageId;
    private Long    userId;
    private Long    alertId;
    private String  alertName;
    private String  content;
    private Integer isRead;      // 0未读 1已读
    private Date    createTime;
}