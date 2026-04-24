package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.User;
import com.crawler.entity.dto.*;
import com.crawler.service.SpecialAlertService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/specialAlert")
public class SpecialAlertController {
    @Resource
    private SpecialAlertService specialAlertService;

    //  显示预警专题列表（分页）,支持筛选功能
    @PostMapping("/pageList")
    public Result pageList(HttpServletRequest request,
                           @RequestBody SpecialAlertPageQueryDto queryDto) {
        // userId 从 token 中获取，不由前端传入
        User currentUser = (User) request.getAttribute("currentUser");
        queryDto.setUserId(Long.valueOf(currentUser.getUserId()));

        Map<String, Object> data = specialAlertService.pageList(queryDto);

        return Result.success(data);
    }

    // 根据预警Id查询预警配置信息
    @GetMapping("/searchById")
    public Result searchById(HttpServletRequest request,
                          @RequestParam Integer alertId){

        SpecialAlertSetting specialAlertInfo = specialAlertService.getSpecialAlertById(alertId);

        return Result.success(specialAlertInfo);

    }

    //  新增预警专题
    @PostMapping("/create")
    public Result create(HttpServletRequest request,
                         @RequestBody SpecialAlertCreateDto createDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        createDto.setUserId(Long.valueOf(currentUser.getUserId()));

        Map<String, Object> data = specialAlertService.create(createDto);
        return Result.success(data);
    }

    //  编辑预警专题（专题须处于关闭状态）
    @PostMapping("/edit")
    public Result edit(@RequestBody SpecialAlertEditDto editDto) {
        Map<String, Object> data = specialAlertService.edit(editDto);
        return Result.success(data);
    }

    //  启用 / 关闭预警专题（异步通知Python）
    @GetMapping("/triggerState")
    public Result triggerState(@RequestParam Integer alertId) {
        Map<String, Object> data = specialAlertService.toggleTriggerState(alertId);
        // 返回最新状态码
        return Result.success(data);
    }

    //  删除预警专题（专题须处于关闭状态）
    @GetMapping("/alertDelete")
    public Result alertDelete(@RequestParam Integer alertId) {
        return specialAlertService.delete(alertId);
    }

    // 查询用户可见的预警专题
    @GetMapping("/searchAllAlert")
    public Result searchAllAlert(HttpServletRequest request){
        User currentUser = (User) request.getAttribute("currentUser");
        // TODO 这里需要根据用户权限重新设计
        Map<String, Object> data = specialAlertService.searchAllAlert(currentUser.getUserId());
        return Result.success(data);
    }

    //  显示舆情消息列表
    @PostMapping("/infoList")
    public Result infoList(@RequestBody SpecialAlertInfoDto queryDto) {
        Map<String, Object> data = specialAlertService.infoList(queryDto);
        return Result.success(data);
    }

    //  删除舆情消息
    @PostMapping("/infoDelete")
    public Result infoDelete(@RequestBody Map<String, Object> body) {
        Long newsId = Long.parseLong(String.valueOf(body.get("newsId")));
        return specialAlertService.infoDelete(newsId);
    }
}
