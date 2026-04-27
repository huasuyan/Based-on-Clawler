package com.crawler.controller;

import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.User;
import com.crawler.entity.dto.special_alert.SpecialAlertCreateDto;
import com.crawler.entity.dto.special_alert.SpecialAlertEditDto;
import com.crawler.entity.dto.special_alert.SpecialAlertInfoDto;
import com.crawler.entity.dto.special_alert.SpecialAlertPageQueryDto;
import com.crawler.mapper.UserMapper;
import com.crawler.service.SpecialAlertService;
import com.crawler.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/specialAlert")
public class SpecialAlertController {
    @Resource
    private SpecialAlertService specialAlertService;

    @Resource
    private UserService userService;

    //  显示预警专题列表（分页）,支持筛选功能
    @PostMapping("/pageList")
    @RequirePermission(module = "alert", action = "alert_select")
    public Result pageList(HttpServletRequest request,
                           @RequestBody SpecialAlertPageQueryDto queryDto) {
        // userId 从 token 中获取，不由前端传入
        User currentUser = (User) request.getAttribute("currentUser");
        // 用户可见性列表
        List<Long> userIdList = userService.getUserList(currentUser);
        queryDto.setUserIdList(userIdList);

        Map<String, Object> data = specialAlertService.pageList(queryDto);

        return Result.success(data);
    }

    // 根据预警Id查询预警配置信息
    @GetMapping("/searchById")
    @RequirePermission(module = "alert", action = "alert_update")
    public Result searchById(HttpServletRequest request,
                          @RequestParam Integer alertId){

        SpecialAlertSetting specialAlertInfo = specialAlertService.getSpecialAlertById(alertId);

        return Result.success(specialAlertInfo);

    }

    //  新增预警专题
    @PostMapping("/create")
    @RequirePermission(module = "alert", action = "alert_insert")
    public Result create(HttpServletRequest request,
                         @RequestBody SpecialAlertCreateDto createDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        createDto.setUserId(Long.valueOf(currentUser.getUserId()));

        Map<String, Object> data = specialAlertService.create(createDto);
        return Result.success(data);
    }

    //  编辑预警专题（专题须处于关闭状态）
    @PostMapping("/edit")
    @RequirePermission(module = "alert", action = "alert_update")
    public Result edit(@RequestBody SpecialAlertEditDto editDto) {
        Map<String, Object> data = specialAlertService.edit(editDto);
        return Result.success(data);
    }

    //  启用 / 关闭预警专题（异步通知Python）
    @GetMapping("/triggerState")
    @RequirePermission(module = "alert", action = "alert_update")
    public Result triggerState(@RequestParam Integer alertId) {
        Map<String, Object> data = specialAlertService.toggleTriggerState(alertId);
        // 返回最新状态码
        return Result.success(data);
    }

    //  删除预警专题（专题须处于关闭状态）
    @RequirePermission(module = "alert", action = "alert_delete")
    @GetMapping("/alertDelete")
    public Result alertDelete(@RequestParam Integer alertId) {
        return specialAlertService.delete(alertId);
    }

    // 查询用户可见的预警专题
    @GetMapping("/searchAllAlert")
    @RequirePermission(module = "alert", action = "alert_select")
    public Result searchAllAlert(HttpServletRequest request){
        User currentUser = (User) request.getAttribute("currentUser");
        // 获取用户当前权限下可以访问数据的用户列表
        List<Long> userIdList = userService.getUserList(currentUser);
        // 根据用户列表查询预警信息
        Map<String, Object> data = specialAlertService.searchAllAlert(userIdList);
        return Result.success(data);
    }

    //  显示舆情消息列表
    @PostMapping("/infoList")
    @RequirePermission(module = "alert", action = "alert_select")
    public Result infoList(@RequestBody SpecialAlertInfoDto queryDto) {
        Map<String, Object> data = specialAlertService.infoList(queryDto);
        return Result.success(data);
    }

    //  删除舆情消息
    @PostMapping("/infoDelete")
    @RequirePermission(module = "alert", action = "alert_delete")
    public Result infoDelete(@RequestBody Map<String, Object> body) {
        Long newsId = Long.parseLong(String.valueOf(body.get("newsId")));
        return specialAlertService.infoDelete(newsId);
    }
}
