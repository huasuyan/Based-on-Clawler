package com.crawler.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.crawler.entity.Crawler;
import com.crawler.entity.Result;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerUpdateDto;
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

    @Value("${crawler.config.jobGroup}")
    private Integer jobGroup;

    @Value("${crawler.config.executorRouteStrategy}")
    private String executorRouteStrategy;

    @Value("${crawler.config.misfireStrategy}")
    private String misfireStrategy;

    @Value("${crawler.config.executorBlockStrategy}")
    private String executorBlockStrategy;

    @Value("${crawler.config.executorTimeout}")
    private Integer executorTimeout;

    @Value("${crawler.config.executorFailRetryCount}")
    private Integer executorFailRetryCount;

    @Override
    public List<CrawlerDto> pageList(CrawlerPageQueryDTO queryDTO) {
        Map<String,Object> map = new HashMap<>();
        map.put("author",queryDTO.getUserId().toString());
        map.put("offset",(queryDTO.getPageNum()-1)*queryDTO.getPageSize());
        map.put("pagesize",queryDTO.getPageSize());
        map.put("jobGroup",jobGroup);
        map.put("triggerStatus",queryDTO.getTriggerStatus()!=null?queryDTO.getTriggerStatus():-1);
        map.put("executorHandler","");
        map.put("jobDesc","");

        Map<String,Object> res = xxlJobUtil.doGet("/jobinfo/pageList", map);
        Map<String,Object> data = (Map<String, Object>) res.get("data");
        List<XxlJobInfo> XxlJobInfoList = JSONUtil.toList(JSONUtil.parseArray(data.get("data")), XxlJobInfo.class);;
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

    @Override
    public Result updateCrawler(CrawlerUpdateDto crawlerUpdateDto) {
        // 取出该爬虫的原本的所有参数
        CrawlerDto crawlerDto = getJobInfo(crawlerUpdateDto.getCrawlerId());
        // 修改变化的数据
        if (crawlerUpdateDto.getCrawlerName() !=null && !crawlerUpdateDto.getCrawlerName().equals(crawlerDto.getCrawlerName())) {
            crawlerDto.setCrawlerName(crawlerUpdateDto.getCrawlerName());
            // 修改crawler表
            crawlerMapper.updateCrawlerName(crawlerUpdateDto.getCrawlerId(),crawlerDto.getCrawlerName());
        }
        if (crawlerUpdateDto.getScheduleType() !=null && !crawlerUpdateDto.getScheduleType().equals(crawlerDto.getScheduleType())) {
            crawlerDto.setScheduleType(crawlerUpdateDto.getScheduleType());
        }
        if (crawlerUpdateDto.getScheduleConf() !=null && !crawlerUpdateDto.getScheduleConf().equals(crawlerDto.getScheduleConf())) {
            crawlerDto.setScheduleConf(crawlerUpdateDto.getScheduleConf());
        }
        if (crawlerUpdateDto.getConfigMethod() !=null && !crawlerUpdateDto.getConfigMethod().equals(crawlerDto.getConfigMethod())) {
            crawlerDto.setConfigMethod(crawlerUpdateDto.getConfigMethod());
            // 修改crawler表
            crawlerMapper.updateConfigMethod(crawlerUpdateDto.getCrawlerId(),crawlerDto.getConfigMethod());
        }
        if (crawlerUpdateDto.getTriggerStatus() !=null && !crawlerUpdateDto.getTriggerStatus().equals(crawlerDto.getTriggerStatus())) {
            crawlerDto.setTriggerStatus(crawlerUpdateDto.getTriggerStatus());
        }


        // 构建map给xxl-job调度中心发请求修改xxl-job的数据
        Map<String,Object> map = new HashMap<>();
        // 必须参数
        map.put("id",crawlerUpdateDto.getCrawlerId());
        map.put("jobGroup",jobGroup);
        map.put("jobDesc",crawlerDto.getCrawlerName());
        map.put("author",crawlerUpdateDto.getUserId());
        map.put("scheduleType",crawlerDto.getScheduleType());
            // 如果调度类型为CRON则必须传入CRON表达式
        try{
            if(crawlerDto.getScheduleType().equals("CRON")) {
                map.put("scheduleConf", crawlerDto.getScheduleConf());
            }else{
                map.put("scheduleConf", "");
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.error("请输入正确的CRON表达式！");
        }
        map.put("executorRouteStrategy",executorRouteStrategy);
        map.put("misfireStrategy",misfireStrategy);
        map.put("executorBlockStrategy",executorBlockStrategy);
        map.put("executorTimeout",executorTimeout);
        map.put("executorFailRetryCount",executorFailRetryCount);
        map.put("executorHandler","");

        // 后端返回{code\data\msg}
        Map<String,Object> res = xxlJobUtil.doPostForm("/jobinfo/update",map);
        // 校验是否修改成功
        if (res.get("msg").equals("Success")) {
            return Result.success();
        }else{
            throw new RuntimeException("爬虫修改失败，请重试！");
        }

    }

    public Result executeCrawler(Integer jobId) {
        Map<String,Object> map = new HashMap<>();
        map.put("id",jobId);
        map.put("executorParam","");
        map.put("addressList","");
        Map<String,Object> res = xxlJobUtil.doGet("/jobinfo/trigger",map);
        if(res.get("msg").equals("Success")){
            return Result.success();
        }
        throw new RuntimeException("爬虫执行失败，请重试！");
    }

    public Result activateCrawler(Integer jobId) {
        Map<String,Object> map = new HashMap<>();
        List<Integer>  ids = new ArrayList<>();
        ids.add(jobId);
        map.put("ids[]",ids);
        CrawlerDto crawlerDto = getJobInfo(jobId);
        if (crawlerDto == null) throw new RuntimeException("爬虫不存在！");
        else{
            if(crawlerDto.getTriggerStatus() == 1){
                //停止
                Map<String,Object> res = xxlJobUtil.doGet("/jobinfo/stop",map);
                if(res.get("msg").equals("Success")){
                    return Result.success();
                }
                throw new RuntimeException("爬虫停止失败，请重试！");
            }
            else if(crawlerDto.getTriggerStatus() == 0){
                //激活
                Map<String,Object> res = xxlJobUtil.doGet("/jobinfo/start",map);
                if(res.get("msg").equals("Success")){
                    return Result.success();
                }
                throw new RuntimeException("爬虫激活失败，请重试！");
            }
            else throw new RuntimeException("爬虫状态异常！");
        }
    }

    public Result deleteCrawler(Integer jobId) {
        Map<String,Object> map = new HashMap<>();
        List<Integer>  ids = new ArrayList<>();
        ids.add(jobId);
        map.put("ids[]",ids);
        Map<String,Object> res = xxlJobUtil.doGet("/jobinfo/delete",map);
        if(res.get("msg").equals("Success")){
            crawlerMapper.deleteByCrawlerId(jobId);
            return Result.success();
        }
        throw new RuntimeException("爬虫删除失败，请重试！");
    }
}
