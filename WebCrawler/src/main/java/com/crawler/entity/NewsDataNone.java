package com.crawler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsDataNone {
    private Integer crawlerId;
    private String title;
    private String content;
    private String publishTime;
    private String source;
    private String url;
    private String picUrl;


}
