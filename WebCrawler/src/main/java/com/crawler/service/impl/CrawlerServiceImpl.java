package com.crawler.service.impl;

import com.crawler.entity.Crawler;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.mapper.CrawlerMapper;
import com.crawler.mapper.XxlJobInfoMapper;
import com.crawler.service.CrawlerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;

    @Resource
    private CrawlerMapper crawlerMapper;

    @Override
    public List<CrawlerDto> pageList(CrawlerPageQueryDTO queryDTO) {
        return List.of();
    }

    @Override
    public CrawlerDto getJobInfo(String jobId) {
        XxlJobInfo jobInfo = xxlJobInfoMapper.selectByJobId(Integer.parseInt(jobId));
        Crawler crawler = crawlerMapper.selectByCrawlerId(jobId);
        CrawlerDto crawlerDto = new CrawlerDto();
        crawlerDto.setCrawlerName(crawler.getCrawlerName());
        crawlerDto.setCrawlerId(Integer.parseInt(jobId));
        crawlerDto.setConfigMethod(crawler.getConfigMethod());
        crawlerDto.setScheduleConf(jobInfo.getScheduleConf());
        crawlerDto.setScheduleType(jobInfo.getScheduleType());
        if(jobInfo.getTriggerLastTime() != 0){
            // 上次执行时间不为0，说明任务已执行过,则设置上次执行时间
            crawlerDto.setTriggerLastTime(new Date(jobInfo.getTriggerLastTime()));
        }else{
            // 上次执行时间为0，说明任务未执行过,则设置上次执行时间为null
            crawlerDto.setTriggerLastTime(null);
        }
        if(jobInfo.getTriggerNextTime() != 0){
            // 下次执行时间不为0，说明任务已执行过,则设置下次执行时间
            crawlerDto.setTriggerNextTime(new Date(jobInfo.getTriggerNextTime()));
        }else{
            // 下次执行时间为0，说明任务未执行过,则设置下次执行时间为null
            crawlerDto.setTriggerNextTime(null);
        }
        crawlerDto.setJobDesc(jobInfo.getJobDesc());
        crawlerDto.setUpdateTime(jobInfo.getUpdateTime());
        crawlerDto.setTriggerStatus(jobInfo.getTriggerStatus());
        return crawlerDto;
    }
}
