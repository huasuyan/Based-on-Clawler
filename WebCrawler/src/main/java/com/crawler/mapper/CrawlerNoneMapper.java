package com.crawler.mapper;

import com.crawler.entity.CrawlerNoneCreate;

import com.crawler.entity.NewsDataNone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrawlerNoneMapper {

    void insert(CrawlerNoneCreate crawler);

    CrawlerNoneCreate selectById(Integer crawlerId);

    void updateById(CrawlerNoneCreate task);

    List<NewsDataNone> selectData(Integer crawlerId);

    void insertData(@Param("dataList") List<NewsDataNone> dataList);
}
