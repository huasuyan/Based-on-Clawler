package com.crawler.controller;


import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerUpdateDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.service.CrawlerService;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result pageList(HttpServletRequest request, @RequestBody CrawlerPageQueryDTO queryDTO) {
        // 取用户信息
        User currentUser = (User) request.getAttribute("currentUser");
        queryDTO.setUserId(Long.valueOf(currentUser.getUserId()));
        // 分页查询
        List<CrawlerDto> crawlerList = crawlerService.pageList(queryDTO);
        return Result.success(crawlerList);
    }

    /**
     * 编辑爬虫基本信息
     */
    @PostMapping("/update")
    public Result update(HttpServletRequest request, @RequestBody CrawlerUpdateDto crawlerUpdateDto){
        //取userId
        User currentUser = (User) request.getAttribute("currentUser");
        crawlerUpdateDto.setUserId(Long.valueOf(currentUser.getUserId()));

        //调用Service
        crawlerService.updateCrawler(crawlerUpdateDto);

        return Result.success();
    }


}
