package com.crawler.mapper;

import com.crawler.entity.Crawler;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CrawlerMapper {
    Crawler selectByCrawlerId(Integer crawlerId);

    void insert(Crawler crawler);

    List<Crawler> selectByCrawlerName(String crawlerName);

    void updateCrawlerName(Integer crawlerId, String crawlerName);

    void updateConfigMethod(Integer crawlerId, Integer configMethod);
}
