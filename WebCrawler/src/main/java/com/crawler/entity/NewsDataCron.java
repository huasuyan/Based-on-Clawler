package com.crawler.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NewsDataCron {
    private Integer crawlerId;
    private String title;
    private String content;
    private Date publishTime;
    private String source;
    private String url;
    private String picUrl;

    public NewsDataCron(NewsDataCron c) {
        this.crawlerId    = c.getCrawlerId();
        this.title  = c.getTitle();
        this.content = c.getContent();
        this.publishTime = c.getPublishTime();
        this.source    = c.getSource();
        this.url        = c.getUrl();
        this.picUrl    = c.getPicUrl();
    }

    public NewsDataCron() {}

}