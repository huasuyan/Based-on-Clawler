package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.dto.*;
import com.crawler.service.CrawlerCronService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 舆情预警专题测试 Controller
 *
 * 用途：绕过 JWT 拦截器，直接通过 userId 参数测试业务逻辑
 * 路径：/test/crawlerCron/**（已在 WebMvcConfig 白名单中）
 *
 * 注意：仅用于开发阶段测试，上线前删除此文件
 */
@Slf4j
@RestController
@RequestMapping("/test/crawlerCron")
public class CrawlerCronTestController {

    @Resource
    private CrawlerCronService crawlerCronService;

    // ----------------------------------------------------------------
    //  分页列表
    //  POST /test/crawlerCron/pageList
    //  Body: {"userId":1,"pageNum":1,"pageSize":10}
    // ----------------------------------------------------------------
    // ----------------------------------------------------------------
    //  多条件查询
    //  POST /test/crawlerCron/pageList
    //  Body: {"userId":1,"pageNum":1,"pageSize":10,"crawlerName":"烟草","triggerState":0}
    // ----------------------------------------------------------------
    @PostMapping("/pageList")
    public Result pageList(@RequestBody CrawlerCronPageQueryDto queryDto) {
        Map<String, Object> list = crawlerCronService.pageList(queryDto);
        return Result.success(list);
    }

    // ----------------------------------------------------------------
    //  新增专题
    //  POST /test/crawlerCron/create
    //  Body: 见下方注释
    // ----------------------------------------------------------------
    @PostMapping("/create")
    public Result create(@RequestBody CrawlerCronCreateDto createDto) {
        /*
        测试Body示例：
        {
            "userId": 1,
            "crawlerName": "Apifox测试专题",
            "targetSource": "xinhuanet",
            "keyWord": "{\"keywordGroups\":[[\"测试词A\",\"测试词B\"],[\"测试词C\"]]}",
            "params": "{\"searchFields\":1,\"sortField\":0}",
            "frequency": 0,
            "alertTrigger": null,
            "timeRange": null,
            "alertMethod": 1,
            "dedupEnable": 1
        }
        */
        Map<String, Object> data = crawlerCronService.create(createDto);
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  编辑专题（专题须处于关闭状态）
    //  POST /test/crawlerCron/edit
    //  Body: {"crawlerId":1,"crawlerName":"修改后名称","frequency":1,...}
    // ----------------------------------------------------------------
    @PostMapping("/edit")
    public Result edit(@RequestBody CrawlerCronEditDto editDto) {
        Map<String, Object> data = crawlerCronService.edit(editDto);
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  启用/关闭专题（不连Python只测DB状态变化）
    //  POST /test/crawlerCron/triggerState
    //  Body: {"crawlerId":1}
    // ----------------------------------------------------------------
    @GetMapping("/triggerState")
    public Result triggerState(@RequestParam Integer crawlerId) {
        Map<String, Object> data = crawlerCronService.toggleTriggerState(crawlerId);
        // 返回最新状态码
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  删除专题（专题须处于关闭状态）
    //  POST /test/crawlerCron/crawlerDelete
    //  Body: {"crawlerId":1}
    // ----------------------------------------------------------------
    @GetMapping("/crawlerDelete")
    public Result crawlerDelete(@RequestParam Integer crawlerId) {
        return crawlerCronService.delete(crawlerId);
    }

    // ----------------------------------------------------------------
    //  预警信息文件列表（需要本地先有结果文件才能看到数据）
    //  POST /test/crawlerCron/infoList
    //  Body: {"crawlerId":1,"pageNum":1,"pageSize":20}
    // ----------------------------------------------------------------
    @PostMapping("/infoList")
    public Result infoList(@RequestBody Map<String, Object> body) {
        Integer crawlerId = (Integer) body.get("crawlerId");
        Integer pageNum  = body.get("pageNum")  != null ? (Integer) body.get("pageNum")  : 1;
        Integer pageSize = body.get("pageSize") != null ? (Integer) body.get("pageSize") : 20;
        Map<String, Object> data = crawlerCronService.infoList(crawlerId, pageNum, pageSize);
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  预警信息文件详情
    //  POST /test/crawlerCron/info
    //  Body: {"crawlerId":1,"info":"2026-04-20-10-30-00.json"}
    // ----------------------------------------------------------------
    @PostMapping("/info")
    public Result info(@RequestBody Map<String, Object> body) {
        Integer crawlerId   = (Integer) body.get("crawlerId");
        String infoFileName = (String)  body.get("info");
        Map<String, Object> data = crawlerCronService.info(crawlerId, infoFileName);
        return Result.success(data);
    }

    // ----------------------------------------------------------------
    //  删除预警信息文件
    //  POST /test/crawlerCron/infoDelete
    //  Body: {"crawlerId":1,"info":"2026-04-20-10-30-00.json"}
    // ----------------------------------------------------------------
    @PostMapping("/infoDelete")
    public Result infoDelete(@RequestBody Map<String, Object> body) {
        Integer crawlerId   = (Integer) body.get("crawlerId");
        String infoFileName = (String)  body.get("info");
        return crawlerCronService.infoDelete(crawlerId, infoFileName);
    }
}
