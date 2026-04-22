package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.*;
import com.crawler.service.CrawlerCronService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/crawlerCron")
public class CrawlerCronController {
    @Resource
    private CrawlerCronService crawlerCronService;

    //  显示预警专题列表（分页）,支持筛选功能
    @PostMapping("/pageList")
    public Result pageList(HttpServletRequest request,
                           @RequestBody CrawlerCronPageQueryDto queryDto) {
        // userId 从 token 中获取，不由前端传入
        User currentUser = (User) request.getAttribute("currentUser");
        queryDto.setUserId(Long.valueOf(currentUser.getUserId()));

        Map<String, Object> list = crawlerCronService.pageList(queryDto);
        return Result.success(list);
    }

    //  新增预警专题
    @PostMapping("/create")
    public Result create(HttpServletRequest request,
                         @RequestBody CrawlerCronCreateDto createDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        createDto.setUserId(Long.valueOf(currentUser.getUserId()));

        Map<String, Object> data = crawlerCronService.create(createDto);
        return Result.success(data);
    }

    //  编辑预警专题（专题须处于关闭状态）
    @PostMapping("/edit")
    public Result edit(@RequestBody CrawlerCronEditDto editDto) {
        Map<String, Object> data = crawlerCronService.edit(editDto);
        return Result.success(data);
    }

    //  启用 / 关闭预警专题（异步通知Python）
    @GetMapping("/triggerState")
    public Result triggerState(@RequestParam Integer crawlerId) {
        Map<String, Object> data = crawlerCronService.toggleTriggerState(crawlerId);
        // 返回最新状态码
        return Result.success(data);
    }

    //  删除预警专题（专题须处于关闭状态）
    @GetMapping("/crawlerDelete")
    public Result crawlerDelete(@RequestParam Integer crawlerId) {
        return crawlerCronService.delete(crawlerId);
    }

    //  显示舆情消息列表
    @PostMapping("/infoList")
    public Result infoList(@RequestBody CrawlerCronInfoDto queryDto) {
        Map<String, Object> data = crawlerCronService.infoList(queryDto);
        return Result.success(data);
    }

    //  删除舆情消息
    @PostMapping("/infoDelete")
    public Result infoDelete(@RequestBody Map<String, Object> body) {
        Integer crawlerId = (Integer) body.get("crawlerId");
        String url = (String) body.get("url");
        return crawlerCronService.infoDelete(crawlerId, url);
    }
}
