package com.crawler.controller;


import com.crawler.entity.CrawlerNoneResult;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.CrawlerCreateDto;
import com.crawler.entity.dto.ExportDto;

import com.crawler.service.CrawlerNoneService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crawlerNone")
public class CrawlerNoneController {

    @Autowired
    private CrawlerNoneService crawlerNoneService;

    @PostMapping("/createAndRun")
    public Result createAndRun(HttpServletRequest request, @RequestBody CrawlerCreateDto crawler){

        User currentUser = (User) request.getAttribute("currentUser");
        crawler.setUserId(Long.valueOf(currentUser.getUserId()));

        Integer crawlerId = crawlerNoneService.createAndRun(crawler);
        return Result.success(crawlerId);
    }

    @GetMapping("/result")
    public Result getResult(@RequestParam Integer crawlerId){
        CrawlerNoneResult result = crawlerNoneService.getTaskResult(crawlerId);
        return Result.success(result);
    }

    @PostMapping("/export")
    public Result export(@RequestBody ExportDto export, HttpServletResponse response) {
        try {
            crawlerNoneService.export(export.getCrawlerId(), export.getExportFormat(), response);
            // 文件流响应，这里不需要返回JSON
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
