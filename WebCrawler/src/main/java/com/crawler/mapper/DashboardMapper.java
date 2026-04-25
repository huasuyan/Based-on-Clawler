package com.crawler.mapper;

import com.crawler.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DashboardMapper {

    // ---- dashboard_stats ----
    DashboardStats selectTodayStats(@Param("statDate") LocalDate statDate,
                                    @Param("statType") String statType);

    void insertOrUpdateStats(DashboardStats stats);

    // ---- dashboard_trend ----
    List<DashboardTrend> selectTrendByDays(@Param("startDate") LocalDate startDate);

    void insertOrUpdateTrend(DashboardTrend trend);

    // ---- dashboard_platform_stats ----
    List<DashboardPlatformStats> selectPlatformStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") String statType);

    void insertOrUpdatePlatformStats(DashboardPlatformStats stats);

    // ---- dashboard_region_stats ----
    List<DashboardRegionStats> selectRegionStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statType") String statType);

    void insertOrUpdateRegionStats(DashboardRegionStats stats);

    // ---- dashboard_hot_words ----
    List<DashboardHotWord> selectHotWords(
            @Param("statDate") LocalDate statDate,
            @Param("statType") String statType,
            @Param("wordType") String wordType,
            @Param("limit") int limit);

    void insertOrUpdateHotWord(DashboardHotWord hotWord);

    void deleteHotWordsByDate(@Param("statDate") LocalDate statDate,
                              @Param("statType") String statType);

    List<String> selectTodayNewsText(@Param("today") LocalDate today,
                                     @Param("limit") int limit);

    // ---- 从 news_data 聚合计算 ----
    Long countTotalNews();
    Long countTotalNewsArticle();   // article_type != 'video'
    Long countTotalNewsVideo();     // article_type = 'video'
    Long countTotalAlertArticle();   // article_type != 'video'
    Long countTotalAlertVideo();     // article_type = 'video'
    Long countTodayNews(@Param("today") LocalDate today);
    Long countTodayNewsArticle(@Param("today") LocalDate today);
    Long countTodayNewsVideo(@Param("today") LocalDate today);
    Long countYesterdayNews(@Param("yesterday") LocalDate yesterday);

    // ---- 从 special_alert_setting 聚合 ----
    Long countTotalAlert();
    Long countTodayAlert(@Param("today") LocalDate today);
    Long countYesterdayAlert(@Param("yesterday") LocalDate yesterday);
    Integer countTodayAlertByLevel(@Param("today") LocalDate today,
                                   @Param("level") Integer level);

    // ---- 平台聚合（从 news_data + clear_data） ----
    List<DashboardPlatformStats> aggregatePlatformStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ---- 地域聚合 ----
    List<DashboardRegionStats> aggregateRegionStats(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ---- 热词聚合（简单按 title 分词近似统计） ----
    List<DashboardHotWord> aggregateHotWords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit);
}