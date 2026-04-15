package com.crawler.mapper;

import com.crawler.entity.Crawler;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrawlerMapper {
    Crawler selectByCrawlerId(String crawlerId);

}
