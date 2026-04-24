package com.crawler.service.impl;

import com.crawler.entity.AlertTabulate;
import com.crawler.entity.dto.ClearNewsData;
import com.crawler.entity.dto.SpecialAlertInfoDto;
import com.crawler.mapper.AlertTabulateMapper;
import com.crawler.mapper.NewsDataMapper;
import com.crawler.service.DataTabulateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataTabulateServiceImpl implements DataTabulateService {

    @Resource
    private NewsDataMapper newsDataMapper;

    @Resource
    private AlertTabulateMapper alertTabulateMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AlertTabulate countSpecialAlert(Integer alertId) {


        // 取数据
        List<ClearNewsData> allData = getAllNewsData(alertId);

        // 统计数据
        AlertTabulate tabulate = buildStatistics(alertId, allData);

        // 存数据库
        alertTabulateMapper.insert(tabulate);

        return tabulate;
    }

    private List<ClearNewsData> getAllNewsData(Integer alertId) {
        List<ClearNewsData> allData = new ArrayList<>();

        // 固定每页大小（可改大一点，更快）
        int pageSize = 100;
        int pageNum = 1;

        while (true) {
            SpecialAlertInfoDto dto = new SpecialAlertInfoDto();
            dto.setAlertId(alertId);
            dto.setPageNum(pageNum);
            dto.setPageSize(pageSize);

            // 查询一页
            List<ClearNewsData> pageList = newsDataMapper.infoList(dto);

            // 没有数据了，退出循环
            if (pageList == null || pageList.isEmpty()) {
                break;
            }

            // 加入总集合
            allData.addAll(pageList);

            // 下一页
            pageNum++;
        }

        return allData;
    }

    public AlertTabulate buildStatistics(Integer getAlertId, List<ClearNewsData> allData) {
        Long alertId = getAlertId.longValue();

        AlertTabulate stats = new AlertTabulate();
        stats.setAlertId(alertId);
        stats.setCreateTime(LocalDateTime.now());

        // ====================== 1. 统计文章总数 ======================
        int totalArticle = allData.size();
        stats.setTotalArticle(totalArticle);

        // ====================== 2. 情感统计（敏感/中性/非敏感） ======================
        // 规则：
        // sentimentType: -1=负面 → 敏感
        //               0=中性  → 中性
        //               1=正面 → 非敏感
        int totalSensitive = 0;
        int totalNeutral = 0;
        int totalNonSensitive = 0;

        for (ClearNewsData data : allData) {
            Integer type = data.getSentimentType();
            if (type == null) continue;

            if (type == -1) {
                totalSensitive++;
            } else if (type == 0) {
                totalNeutral++;
            } else if (type == 1) {
                totalNonSensitive++;
            }
        }

        stats.setTotalSensitive(totalSensitive);
        stats.setTotalNeutral(totalNeutral);
        stats.setTotalNonSensitive(totalNonSensitive);

        // ====================== 3. 地域分布统计（图表数据） ======================
        // 按 region 分组统计数量
        Map<String, Long> regionCount = allData.stream()
                .filter(data -> data.getRegion() != null && !data.getRegion().isBlank())
                .collect(Collectors.groupingBy(
                        ClearNewsData::getRegion,
                        Collectors.counting()
                ));

        // 组装成前端需要的 [{region:"xx", publishCount:xx}]
        List<Map<String, Object>> chartList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : regionCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("region", entry.getKey());
            item.put("publishCount", entry.getValue().intValue());
            chartList.add(item);
        }

        // ====================== 4. 地域排名（带敏感数） ======================
        // 按地区分组 → 统计总数 + 敏感数
        Map<String, List<ClearNewsData>> groupByRegion = allData.stream()
                .filter(data -> data.getRegion() != null && !data.getRegion().isBlank())
                .collect(Collectors.groupingBy(ClearNewsData::getRegion));

        List<Map<String, Object>> rankList = new ArrayList<>();

        // 1. 先把分组数据转成临时列表，方便排序
        List<Map.Entry<String, List<ClearNewsData>>> tempList = new ArrayList<>(groupByRegion.entrySet());

        // 2. 按照 total 数量 降序排序（total 越大越靠前）
        Collections.sort(tempList, (o1, o2) -> {
            int total1 = o1.getValue().size();
            int total2 = o2.getValue().size();
            return Integer.compare(total2, total1); // 降序
        });

        // 3. 遍历排序后的列表，生成正确排名
        int rank = 1;
        for (Map.Entry<String, List<ClearNewsData>> entry : tempList) {
            String region = entry.getKey();
            List<ClearNewsData> list = entry.getValue();

            int total = list.size();
            int sensitive = (int) list.stream()
                    .filter(d -> d.getSentimentType() != null && d.getSentimentType() == -1)
                    .count();

            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("region", region);
            item.put("publishCount", total);
            item.put("sensitiveCount", sensitive);
            rankList.add(item);
        }

        // ====================== 5. 转 JSON 字符串存入 AlertTabulate ======================
        try {
            String regionChartJson = objectMapper.writeValueAsString(chartList);
            String regionRankJson = objectMapper.writeValueAsString(rankList);

            stats.setRegionChart(regionChartJson);
            stats.setRegionRank(regionRankJson);
        } catch (JsonProcessingException e) {
            stats.setRegionChart("[]");
            stats.setRegionRank("[]");
            e.printStackTrace();
        }

        return stats;
    }
}
