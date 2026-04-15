package com.crawler.controller;


import com.crawler.entity.Result;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crawlers")
public class CrawlerController {

    @Resource
    private XxlJobUtil xxlJobUtil;

    @PostMapping("/searchByJobId")
    public Result pageList(@RequestParam Integer jobId) {
        CrawlerDto crawlerInfo = xxlJobUtil.getJobInfo(jobId.toString());
        return Result.success(crawlerInfo);
    }

}
