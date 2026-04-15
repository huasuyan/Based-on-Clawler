package com.crawler.controller;


import com.crawler.entity.Result;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.service.CrawlerService;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crawlers")
public class CrawlerController {

    @Resource
    private XxlJobUtil xxlJobUtil;

    @Resource
    private CrawlerService crawlerService;
    /**
     * 根据jobId查询爬虫信息
     * @param jobId
     * @return
     */
    @PostMapping("/searchByJobId")
    public Result searchByJobId(@RequestParam Integer jobId) {
        CrawlerDto crawlerInfo = crawlerService.getJobInfo(jobId);
        return Result.success(crawlerInfo);
    }

    @PostMapping("/pageList")
    public Result pageList(@RequestBody CrawlerPageQueryDTO queryDTO) {
        // 分页查询
        List<CrawlerDto> crawlerList = crawlerService.pageList(queryDTO);
        return Result.success(crawlerList);
    }


}
