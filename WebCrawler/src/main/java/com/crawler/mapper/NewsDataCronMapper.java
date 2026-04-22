package com.crawler.mapper;

import com.crawler.entity.NewsDataCron;
import com.crawler.entity.dto.CrawlerCronInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsDataCronMapper {
    // 批量插入（忽略重复url）
    void batchInsertIgnore(List<NewsDataCron> newsList);

    // 列表查询
    List<NewsDataCron> infoList(CrawlerCronInfoDto queryDto);

    void delete (@Param("url") String url,@Param("crawlerId") Integer crawlerId);

    NewsDataCron select (@Param("url") String url,@Param("crawlerId") Integer crawlerId);
}
