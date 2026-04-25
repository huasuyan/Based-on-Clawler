package com.crawler.service.impl;

import com.crawler.entity.*;
import com.crawler.entity.dto.dashboard.*;
import com.crawler.mapper.DashboardMapper;
import com.crawler.service.DashboardService;
import com.crawler.util.WordSegmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    private static final DateTimeFormatter TREND_FMT =
            DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public DashboardSummaryDto getSummary() {
        LocalDate today = LocalDate.now();
        DashboardStats stats = dashboardMapper.selectTodayStats(today, "daily");

        // 缓存未命中则实时计算并写入
        if (stats == null) {
            refreshStats();
            stats = dashboardMapper.selectTodayStats(today, "daily");
        }

        // 如果更新后仍无数据则返回空对象
        if (stats == null) {
            return new DashboardSummaryDto();
        }

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setTotalNews(stats.getTotalNews());
        dto.setTotalNewsArticle(stats.getTotalNewsArticle());
        dto.setTotalNewsVideo(stats.getTotalNewsVideo());
        dto.setTotalAlert(stats.getTotalAlert());
        dto.setTotalAlertArticle(stats.getTotalAlertArticle());
        dto.setTotalAlertVideo(stats.getTotalAlertVideo());
        dto.setTotalCase(stats.getTotalCase());
        dto.setCaseInProgress(stats.getCaseInProgress());
        dto.setCaseFinished(stats.getCaseFinished());
        dto.setCaseConversionRate(stats.getCaseConversionRate());
        dto.setTodayNews(stats.getTodayNews());
        dto.setTodayNewsArticle(stats.getTodayNewsArticle());
        dto.setTodayNewsVideo(stats.getTodayNewsVideo());
        dto.setTodayNewsYoy(stats.getTodayNewsYoy());
        dto.setTodayAlert(stats.getTodayAlert());
        dto.setTodayAlertYoy(stats.getTodayAlertYoy());
        dto.setTodayAlertLevel1(stats.getTodayAlertLevel1());
        dto.setTodayAlertLevel2(stats.getTodayAlertLevel2());
        dto.setTodayAlertLevel3(stats.getTodayAlertLevel3());
        return dto;
    }

    @Override
    public DashboardTrendDto getTrend(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<DashboardTrend> trendList =
                dashboardMapper.selectTrendByDays(startDate);

        // 按日期建立 Map，补全缺失日期为0
        Map<LocalDate, DashboardTrend> trendMap = trendList.stream()
                .collect(Collectors.toMap(DashboardTrend::getStatDate, t -> t));

        List<String> dates = new ArrayList<>();
        List<Long> newsCounts = new ArrayList<>();
        List<Integer> alertCounts = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(TREND_FMT));
            DashboardTrend trend = trendMap.get(date);
            newsCounts.add(trend != null ? trend.getNewsCount() : 0L);
            alertCounts.add(trend != null ? trend.getAlertCount() : 0);
        }

        DashboardTrendDto dto = new DashboardTrendDto();
        dto.setDates(dates);
        dto.setNewsCounts(newsCounts);
        dto.setAlertCounts(alertCounts);
        return dto;
    }

    @Override
    public DashboardPlatformDto getPlatformStats(String statType,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {
        if (startDate == null) {
            startDate = resolveStartDate(statType);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<DashboardPlatformStats> list =
                dashboardMapper.selectPlatformStats(startDate, endDate, statType);

        // 预计算表无数据或请求非 daily 类型时，从 news_data 实时聚合
        if (list == null || list.isEmpty()) {
            list = dashboardMapper.aggregatePlatformStats(startDate, endDate);
        }

        // 合并同一平台多天数据
        Map<String, DashboardPlatformStats> merged = new LinkedHashMap<>();
        for (DashboardPlatformStats s : list) {
            merged.merge(s.getSource(), s, (existing, newVal) -> {
                existing.setSensitiveCount(
                        existing.getSensitiveCount() + newVal.getSensitiveCount());
                existing.setNeutralCount(
                        existing.getNeutralCount() + newVal.getNeutralCount());
                existing.setNormalCount(
                        existing.getNormalCount() + newVal.getNormalCount());
                existing.setTotalCount(
                        existing.getTotalCount() + newVal.getTotalCount());
                return existing;
            });
        }

        List<PlatformBarDto> barData = merged.values().stream()
                .sorted((a, b) -> b.getTotalCount() - a.getTotalCount())
                .map(s -> {
                    PlatformBarDto bar = new PlatformBarDto();
                    bar.setSource(s.getSource());
                    bar.setSensitive(s.getSensitiveCount());
                    bar.setNeutral(s.getNeutralCount());
                    bar.setNormal(s.getNormalCount());
                    bar.setTotal(s.getTotalCount());
                    return bar;
                }).collect(Collectors.toList());

        List<PlatformPieDto> pieData = barData.stream()
                .map(b -> {
                    PlatformPieDto pie = new PlatformPieDto();
                    pie.setSource(b.getSource());
                    pie.setValue(b.getTotal());
                    return pie;
                }).collect(Collectors.toList());

        DashboardPlatformDto dto = new DashboardPlatformDto();
        dto.setBarData(barData);
        dto.setPieData(pieData);
        return dto;
    }

    @Override
    public List<RegionRankDto> getRegionStats(String statType,
                                              LocalDate startDate,
                                              LocalDate endDate) {
        if (startDate == null) startDate = resolveStartDate(statType);
        if (endDate == null) endDate = LocalDate.now();

        List<DashboardRegionStats> list =
                dashboardMapper.selectRegionStats(startDate, endDate, statType);

        // 预计算表无数据或请求非 daily 类型时，从 news_data 实时聚合
        if (list == null || list.isEmpty()) {
            list = dashboardMapper.aggregateRegionStats(startDate, endDate);
        }

        // 合并多天同地区数据
        Map<String, Integer> regionMap = new LinkedHashMap<>();
        for (DashboardRegionStats s : list) {
            regionMap.merge(s.getRegion(), s.getNewsCount(), Integer::sum);
        }

        // 排序并转换 DTO
        List<Map.Entry<String, Integer>> sorted = regionMap.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());

        List<RegionRankDto> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            RegionRankDto dto = new RegionRankDto();
            dto.setRank(i + 1);
            dto.setRegion(sorted.get(i).getKey());
            dto.setNewsCount(sorted.get(i).getValue());
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<HotWordDto> getHotWords(String statType, String wordType,
                                        LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = resolveStartDate(statType);
        if (endDate == null) endDate = LocalDate.now();

        List<DashboardHotWord> list = new ArrayList<>();

        // 查询单日时优先使用预计算表
        if (startDate.equals(endDate)) {
            list = dashboardMapper.selectHotWords(startDate, statType, wordType, 50);
        }

        // 预计算无数据或跨多天时，从 news_data 实时聚合
        if (list == null || list.isEmpty()) {
            list = dashboardMapper.aggregateHotWords(startDate, endDate, 50);
        }

        return list.stream().map(hw -> {
            HotWordDto dto = new HotWordDto();
            dto.setWord(hw.getWord());
            dto.setCount(hw.getWordCount());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshStats() {
        log.info("[Dashboard] 开始刷新大屏统计数据...");
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // ---- 1. 计算汇总统计 ----
        Long totalNews = safe(dashboardMapper.countTotalNews());
        Long totalNewsArticle = safe(dashboardMapper.countTotalNewsArticle());
        Long totalNewsVideo = safe(dashboardMapper.countTotalNewsVideo());
        Long totalAlert = safe(dashboardMapper.countTotalAlert());
        // 预警中文章/视频
        Long totalAlertArticle = safe(dashboardMapper.countTotalAlertArticle());
        Long totalAlertVideo = safe(dashboardMapper.countTotalAlertVideo());

        Long todayNews = safe(dashboardMapper.countTodayNews(today));
        Long todayNewsArticle = safe(dashboardMapper.countTodayNewsArticle(today));
        Long todayNewsVideo = safe(dashboardMapper.countTodayNewsVideo(today));
        Long yesterdayNews = safe(dashboardMapper.countYesterdayNews(yesterday));

        Long todayAlert = safe(dashboardMapper.countTodayAlert(today));
        Long yesterdayAlert = safe(dashboardMapper.countYesterdayAlert(yesterday));

        Integer alertLevel1 = safeInt(dashboardMapper.countTodayAlertByLevel(today, 1));
        Integer alertLevel2 = safeInt(dashboardMapper.countTodayAlertByLevel(today, 2));
        Integer alertLevel3 = safeInt(dashboardMapper.countTodayAlertByLevel(today, 3));

        // 同比计算
        BigDecimal todayNewsYoy = calcYoy(todayNews, yesterdayNews);
        BigDecimal todayAlertYoy = calcYoy(todayAlert, yesterdayAlert);

        // 办件数据（若无专属表，此处先写固定值，后续对接实际数据）
        long totalCase = 832L;
        int caseInProgress = 334;
        int caseFinished = 231;
        BigDecimal conversionRate = new BigDecimal("12.2");

        DashboardStats stats = new DashboardStats();
        stats.setStatDate(today);
        stats.setStatType("daily");
        stats.setTotalNews(totalNews);
        stats.setTotalNewsArticle(totalNewsArticle);
        stats.setTotalNewsVideo(totalNewsVideo);
        stats.setTotalAlert(totalAlert);
        stats.setTotalAlertArticle(totalAlertArticle);
        stats.setTotalAlertVideo(totalAlertVideo);
        stats.setTotalCase(totalCase);
        stats.setCaseInProgress(caseInProgress);
        stats.setCaseFinished(caseFinished);
        stats.setCaseConversionRate(conversionRate);
        stats.setTodayNews(todayNews);
        stats.setTodayNewsArticle(todayNewsArticle);
        stats.setTodayNewsVideo(todayNewsVideo);
        stats.setTodayNewsYoy(todayNewsYoy);
        stats.setTodayAlert(todayAlert);
        stats.setTodayAlertYoy(todayAlertYoy);
        stats.setTodayAlertLevel1(alertLevel1);
        stats.setTodayAlertLevel2(alertLevel2);
        stats.setTodayAlertLevel3(alertLevel3);
        dashboardMapper.insertOrUpdateStats(stats);

        // ---- 2. 刷新趋势数据（今天） ----
        DashboardTrend trend = new DashboardTrend();
        trend.setStatDate(today);
        trend.setNewsCount(todayNews);
        trend.setAlertCount(Math.toIntExact(todayAlert));
        dashboardMapper.insertOrUpdateTrend(trend);

        // ---- 3. 刷新平台数据 ----
        refreshPlatformStats(today);

        // ---- 4. 刷新地域数据 ----
        refreshRegionStats(today);

        // ---- 5. 刷新热词数据 ----
        refreshHotWords(today);

        log.info("[Dashboard] 大屏统计数据刷新完成");
    }

    // ---- 私有方法 ----

    private void refreshPlatformStats(LocalDate today) {
        List<DashboardPlatformStats> list =
                dashboardMapper.aggregatePlatformStats(today, today);
        for (DashboardPlatformStats s : list) {
            s.setStatDate(today);
            s.setStatType("daily");
            dashboardMapper.insertOrUpdatePlatformStats(s);
        }
    }

    private void refreshRegionStats(LocalDate today) {
        List<DashboardRegionStats> list =
                dashboardMapper.aggregateRegionStats(today, today);
        for (DashboardRegionStats s : list) {
            s.setStatDate(today);
            s.setStatType("daily");
            dashboardMapper.insertOrUpdateRegionStats(s);
        }
    }

    private void refreshHotWords(LocalDate today) {
        // 1. 删除当天旧热词，避免累积无效词
        dashboardMapper.deleteHotWordsByDate(today, "daily");

        // 2. 获取今天的新闻文本（标题权重×3 + 内容截取）
        List<String> textList = dashboardMapper.selectTodayNewsText(today, 1000);
        if (textList == null || textList.isEmpty()) {
            log.warn("[Dashboard] 今日暂无新闻数据，热词跳过");
            return;
        }

        // 3. 分词统计
        Map<String, Integer> keywordFreq = new HashMap<>();
        Map<String, Integer> entityFreq = new HashMap<>();

        for (String text : textList) {
            // 以空格分割，前半段是标题部分(已拼接3次)，后半段是内容
            // 标题部分: 标题重复3次以获得更高权重
            int firstSpace = text.indexOf(' ');
            String contentPart = text;
            String titlePart = "";

            if (firstSpace > 0) {
                titlePart = text.substring(0, firstSpace);
                contentPart = text.substring(firstSpace + 1);
            }

            // 标题重复3次以强化权重
            String titleWeighted = titlePart + " " + titlePart + " " + titlePart;

            // 分别分词
            Map<String, Integer> titleKw = WordSegmentUtil.segmentKeywords(titleWeighted);
            Map<String, Integer> contentKw = WordSegmentUtil.segmentKeywords(contentPart);
            Map<String, Integer> titleEnt = WordSegmentUtil.segmentEntities(titleWeighted);
            Map<String, Integer> contentEnt = WordSegmentUtil.segmentEntities(contentPart);

            // 合并
            mergeFreq(keywordFreq, titleKw);
            mergeFreq(keywordFreq, contentKw);
            mergeFreq(entityFreq, titleEnt);
            mergeFreq(entityFreq, contentEnt);
        }

        // 4. 取前50写入数据库
        List<DashboardHotWord> hotWords = new ArrayList<>();
        int rank = 0;
        for (Map.Entry<String, Integer> entry : keywordFreq.entrySet()) {
            if (rank++ >= 50) break;
            DashboardHotWord hw = new DashboardHotWord();
            hw.setStatDate(today);
            hw.setStatType("daily");
            hw.setWord(entry.getKey());
            hw.setWordCount(entry.getValue());
            hw.setWordType("keyword");
            hotWords.add(hw);
        }

        rank = 0;
        for (Map.Entry<String, Integer> entry : entityFreq.entrySet()) {
            if (rank++ >= 50) break;
            DashboardHotWord hw = new DashboardHotWord();
            hw.setStatDate(today);
            hw.setStatType("daily");
            hw.setWord(entry.getKey());
            hw.setWordCount(entry.getValue());
            hw.setWordType("entity");
            hotWords.add(hw);
        }

        for (DashboardHotWord hw : hotWords) {
            dashboardMapper.insertOrUpdateHotWord(hw);
        }

        log.info("[Dashboard] 热词刷新完成, keyword={} entity={}",
                Math.min(keywordFreq.size(), 50),
                Math.min(entityFreq.size(), 50));
    }

    private void mergeFreq(Map<String, Integer> target, Map<String, Integer> source) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    private LocalDate resolveStartDate(String statType) {
        return switch (statType) {
            case "weekly"  -> LocalDate.now().minusDays(6);
            case "monthly" -> LocalDate.now().minusDays(29);
            default        -> LocalDate.now();
        };
    }

    private BigDecimal calcYoy(Long today, Long yesterday) {
        if (yesterday == null || yesterday == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((today - yesterday) * 100.0 / yesterday)
                .setScale(1, RoundingMode.HALF_UP);
    }

    private Long safe(Long val) { return val == null ? 0L : val; }
    private Integer safeInt(Integer val) { return val == null ? 0 : val; }
}