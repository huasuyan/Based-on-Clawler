package com.crawler.mapper;

import com.crawler.entity.NewsData;
import com.crawler.entity.dto.SpecialAlertInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsDataMapper {
    // 批量插入（忽略重复url）
    void batchInsertIgnore(List<NewsData> newsList);

    // 列表查询
    List<NewsData> infoList(SpecialAlertInfoDto queryDto);

    void delete (@Param("url") String url,@Param("alertId") Integer alertId);

    NewsData select (@Param("url") String url,@Param("alertId") Integer alertId);
}
