package com.crawler.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.Crawler;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.mapper.CrawlerMapper;
import com.crawler.mapper.XxlJobInfoMapper;
import com.crawler.service.CrawlerService;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;

    @Resource
    private CrawlerMapper crawlerMapper;

    @Resource
    private XxlJobUtil xxlJobUtil;

    @Value("${crawler.pageList.searchMethod}")
    private Integer searchMethod;

    @Override
    public List<CrawlerDto> pageList(CrawlerPageQueryDTO queryDTO) {
        Map<String,Object> map = new HashMap<>();
        map.put("author",queryDTO.getUserId().toString());
        map.put("offset",(queryDTO.getPageNum()-1)*queryDTO.getPageSize());
        map.put("pagesize",queryDTO.getPageSize());
        map.put("jobGroup",10);
        map.put("triggerStatus",queryDTO.getTriggerStatus()!=null?queryDTO.getTriggerStatus():-1);
        map.put("executorHandler","");
        map.put("jobDesc","");

        Object data = xxlJobUtil.doGet("/jobinfo/pageList", map);
        List<XxlJobInfo> XxlJobInfoList = JSONUtil.toList(JSONUtil.parseArray(data), XxlJobInfo.class);
        List<CrawlerDto> crawlerDtoList = new ArrayList<>();
        for (XxlJobInfo xxlJobInfo : XxlJobInfoList) {
            Integer jobId = xxlJobInfo.getId();
            Crawler crawler = crawlerMapper.selectByCrawlerId(jobId);

            // ====================== 筛选开始 ======================
            boolean match = true;

            // 1. 筛选 crawlerName（爬虫名称 模糊匹配）
            String crawlerName = queryDTO.getCrawlerName();
            if(searchMethod.equals(0)){
                if (StrUtil.isNotBlank(crawlerName) && !crawler.getCrawlerName().contains(crawlerName)) {
                    match = false;
                }
            }else if(searchMethod.equals(1)){
                if (StrUtil.isNotBlank(crawlerName) && !crawler.getCrawlerName().equals(crawlerName)) {
                    match = false;
                }
            }


            // 2. 筛选 scheduleType（调度类型，来自 XXL-Job）
            String scheduleType = queryDTO.getScheduleType();
            if (StrUtil.isNotBlank(scheduleType) && !scheduleType.equals(xxlJobInfo.getScheduleType())) {
                match = false;
            }

            // 3. 筛选 configMethod（配置方式，来自 crawler 表）
            Integer configMethod = queryDTO.getConfigMethod();
            if (configMethod != null && !configMethod.equals(crawler.getConfigMethod())) {
                match = false;
            }

            // 4. 筛选 triggerStatus（运行状态，来自 XXL-Job）
            Integer triggerStatus = queryDTO.getTriggerStatus();
            if (triggerStatus != null && !triggerStatus.equals(xxlJobInfo.getTriggerStatus())) {
                match = false;
            }
            // ====================== 筛选结束 ======================

            // 只有全部匹配，才加入结果集
            if (match) {
                crawlerDtoList.add(xxlJobUtil.mergeData(jobId, xxlJobInfo, crawler));
            }
        }

        return crawlerDtoList;
    }

    @Override
    public CrawlerDto getJobInfo(Integer jobId) {
        XxlJobInfo jobInfo = xxlJobInfoMapper.selectByJobId(jobId);
        Crawler crawler = crawlerMapper.selectByCrawlerId(jobId);
        return xxlJobUtil.mergeData(jobId,jobInfo,crawler);
    }
}
