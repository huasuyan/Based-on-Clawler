package com.crawler.mapper;

import com.crawler.entity.CrawlerCron;
import com.crawler.entity.dto.CrawlerCronPageQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预警专题 Mapper
 */
@Mapper
public interface CrawlerCronMapper {

    // 新增预警专题
    void insert(CrawlerCron crawlerCron);

    // 根据crawlerId查询
    CrawlerCron selectByCrawlerId(Integer crawlerId);

    // 分页列表查询（支持多条件筛选）
    List<CrawlerCron> pageList(CrawlerCronPageQueryDto queryDto);

    // 更新专题信息（编辑接口使用）
    void update(CrawlerCron crawlerCron);

    // 更新启用状态
    void updateTriggerState(@Param("crawlerId") Integer crawlerId,
                            @Param("triggerState") Integer triggerState);

    // 更新运行状态（Python回调使用）
    void updateState(@Param("crawlerId") Integer crawlerId,
                     @Param("state") Integer state);

    // 删除专题
    void deleteByCrawlerId(Integer crawlerId);
}
