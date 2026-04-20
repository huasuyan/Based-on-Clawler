package com.crawler.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CrawlerDto {
    private Integer crawlerId; //爬虫id
    private String crawlerName; //爬虫名称
    private String scheduleConf; //定时表达式
    private String scheduleType;  //定时类型
    private Integer configMethod;  //配置方法
    private Date triggerLastTime; //上次执行时间
    private Date triggerNextTime;  //下次执行时间
    private String jobDesc; //任务描述
    private Date updateTime; // 更新时间
    private Integer triggerStatus; //执行状态
    private String crawlerSource; //爬虫代码
}
