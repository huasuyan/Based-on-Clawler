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
}