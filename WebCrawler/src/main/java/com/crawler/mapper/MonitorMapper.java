package com.crawler.mapper;

import com.crawler.entity.dto.ClearNewsData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 舆情监测模块 Mapper
 * 提供文章查询、关键词匹配预警/报告专题等数据访问方法
 */
@Mapper
public interface MonitorMapper {

    // 根据关键词模糊匹配预警专题名称，获取alert_id列表
    List<Integer> searchAlertIdsByKeyword(@Param("keyWord") String keyWord);

    // 根据关键词模糊匹配报告专题名称，获取special_report_id列表
    List<Long> searchReportIdsByKeyword(@Param("keyWord") String keyWord);

    // 多条件筛选查询文章列表（关联news_data和clear_data）
    List<ClearNewsData> queryArticleList(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("sensitivityLevels") List<Integer> sensitivityLevels,
            @Param("source") String source,
            @Param("region") String region,
            @Param("alertIds") List<Integer> alertIds,
            @Param("reportIds") List<Long> reportIds,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 统计筛选条件下的文章总数（分页用）
    int countArticleList(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("sensitivityLevels") List<Integer> sensitivityLevels,
            @Param("source") String source,
            @Param("region") String region,
            @Param("alertIds") List<Integer> alertIds,
            @Param("reportIds") List<Long> reportIds
    );

    // 根据reportId查询文章列表（支持alertId或specialReportId）
    List<ClearNewsData> queryInfoList(
            @Param("reportId") Long reportId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 统计reportId对应文章总数（分页用）
    int countInfoList(@Param("reportId") Long reportId);

    // 查询所有预警专题列表（用于searchAllReport）
    List<com.crawler.entity.SpecialAlertSetting> selectAllAlerts();

    // 查询所有启用中的报告专题（用于searchAllReport）
    List<com.crawler.entity.SpecialReportSetting> selectAllReports();
}
