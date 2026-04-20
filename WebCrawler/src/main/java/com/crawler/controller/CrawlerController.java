package com.crawler.controller;


import com.crawler.entity.Crawler;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.dto.CrawlerUpdateDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.dto.CrawlerUploadDto;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.service.CrawlerService;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        return crawlerService.updateCrawler(crawlerUpdateDto);
    }

    @PostMapping("/execute")
    public Result execute(@RequestParam  Integer jobId){
        //调用Service
        crawlerService.executeCrawler(jobId);
        return Result.success();
    }

    @PostMapping("/activate")
    public Result activate(@RequestParam  Integer jobId){
        //调用Service
        crawlerService.activateCrawler(jobId);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam  Integer jobId){
        //调用Service
        crawlerService.deleteCrawler(jobId);
        return Result.success();
    }

    @PostMapping("/script")
    public Result uploadScript(HttpServletRequest request, @RequestBody CrawlerUploadDto uploadDto) {
        try {
            // 取用户信息
            User currentUser = (User) request.getAttribute("currentUser");
            uploadDto.setUserId(Long.valueOf(currentUser.getUserId()));
            // 调用服务创建爬虫
            boolean result = crawlerService.createCrawlerByScript(uploadDto);

            if (result) {
                return Result.success();
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("上传脚本失败", e);
            return Result.error("上传脚本失败：" + e.getMessage());
        }
    }
}
