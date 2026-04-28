package com.crawler.controller;

import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.case_management.CaseCreateDto;
import com.crawler.entity.dto.case_management.CasePageQueryDto;
import com.crawler.entity.dto.case_management.CaseSubmitTextDto;
import com.crawler.service.CaseManagementService;
import com.crawler.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/case")
public class CaseManagementController {

    @Resource
    private CaseManagementService caseManagementService;

    @Resource
    private UserService userService;

    // 预警记录转为办件
    @PostMapping("/create")
    @RequirePermission(module = "case", action = "case_insert")
    public Result create(HttpServletRequest request,
                         @RequestBody CaseCreateDto createDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        Map<String, Object> data = caseManagementService.create(createDto, currentUser.getUserId());
        return Result.success(data);
    }

    // 条件查询办件列表
    @PostMapping("/pageList")
    @RequirePermission(module = "case", action = "case_select")
    public Result pageList(HttpServletRequest request,@RequestBody CasePageQueryDto queryDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        // 获取用户当前可见的部门列表
        List<Long> deptIdList = userService.getAllDeptIds(currentUser);
        queryDto.setDeptIdList(deptIdList);
        Map<String, Object> data = caseManagementService.pageList(queryDto);
        return Result.success(data);
    }

    // 提交意见
    @PostMapping("/submitText")
    @RequirePermission(module = "case", action = "case_update")
    public Result submitText(HttpServletRequest request,
                             @RequestBody CaseSubmitTextDto submitDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        Map<String, Object> data = caseManagementService.submitText(submitDto, currentUser.getUserId());
        return Result.success(data);
    }

    // 办理办件（校验办理意见后 state -> 1）
    @GetMapping("/process")
    @RequirePermission(module = "case", action = "case_update")
    public Result process(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.process(caseId);
        return Result.success(data);
    }

    // 归档办件（校验归档意见后 state -> 2）
    @GetMapping("/archive")
    @RequirePermission(module = "case", action = "case_update")
    public Result archive(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.archive(caseId);
        return Result.success(data);
    }

    // 关闭办件（校验停用说明后 triggerState -> 0）
    @GetMapping("/close")
    @RequirePermission(module = "case", action = "case_update")
    public Result close(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.close(caseId);
        return Result.success(data);
    }

    // 启用办件（校验启用说明后 triggerState -> 1）
    @GetMapping("/enable")
    @RequirePermission(module = "case", action = "case_update")
    public Result enable(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.enable(caseId);
        return Result.success(data);
    }

    // 标记异常办件（校验异常说明后 state -> 3）
    @GetMapping("/markException")
    @RequirePermission(module = "case", action = "case_update")
    public Result markException(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.markException(caseId);
        return Result.success(data);
    }

    // 查询办件上传信息列表
    @GetMapping("/textList")
    @RequirePermission(module = "case", action = "case_select")
    public Result textList(@RequestParam Long caseId) {
        Map<String, Object> data = caseManagementService.textList(caseId);
        return Result.success(data);
    }
}
