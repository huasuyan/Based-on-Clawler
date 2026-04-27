package com.crawler.mapper;

import com.crawler.entity.NewsData;
import com.crawler.entity.dto.ClearNewsData;
import com.crawler.entity.dto.special_alert.SpecialAlertInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface NewsDataMapper {
    // 列表查询
    List<ClearNewsData> infoList(SpecialAlertInfoDto queryDto);

    void delete (@Param("newsId") Long newsId);

    NewsData select (@Param("newsId") Long newsId);

    // 查询某 alertId 下晚于指定时间的新增数量
    int countNewsByAlertIdAfterTime(@Param("alertId") Long alertId,
                                    @Param("afterTime") Date afterTime);

    // 查询某 alertId 下的全部数据数量（首次执行时使用）
    int countNewsByAlertId(@Param("alertId") Long alertId);

    // 统计总条数（分页用）
    int countPageList(SpecialAlertInfoDto queryDto);

    // 删除某个 alertId 下的所有数据
    int deleteByAlertId(Long alertId);

    // 删除某个 reportId 下的所有数据
    int deleteByReportId(Long reportId);
}
