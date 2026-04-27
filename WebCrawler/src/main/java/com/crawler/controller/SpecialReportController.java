package com.crawler.controller;

import com.crawler.annotation.RequirePermission;
import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.special_report.SpecialReportCreateDto;
import com.crawler.entity.dto.special_report.SpecialReportEditDto;
import com.crawler.entity.dto.special_report.SpecialReportPageQueryDto;
import com.crawler.service.SpecialReportService;
import com.crawler.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/specialReport")
public class SpecialReportController {

    @Resource
    private SpecialReportService specialReportService;
    @Autowired
    private UserService userService;

    // 新增报告专题
    @PostMapping("/create")
    @RequirePermission(module = "report", action = "report_insert")
    public Result create(HttpServletRequest request,
                         @RequestBody SpecialReportCreateDto createDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        createDto.setCreateUserId(Long.valueOf(currentUser.getUserId()));
        Map<String, Object> data = specialReportService.create(createDto);
        return Result.success(data);
    }

    // 编辑报告专题
    @PostMapping("/edit")
    @RequirePermission(module = "report", action = "report_update")
    public Result edit(@RequestBody SpecialReportEditDto editDto) {
        specialReportService.edit(editDto);
        return Result.success();
    }

    // 分页查询报告专题列表
    @PostMapping("/pageList")
    @RequirePermission(module = "report", action = "report_select")
    public Result pageList(HttpServletRequest request,
                           @RequestBody SpecialReportPageQueryDto queryDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        // 获取可见用户列表
        List<Long> userIdList = userService.getUserList(currentUser);

        queryDto.setCreateUserIdList(userIdList);
        Map<String, Object> data = specialReportService.pageList(queryDto);
        return Result.success(data);
    }

    // 删除报告专题（须停用后才能删除）
    @GetMapping("/delete")
    @RequirePermission(module = "report", action = "report_delete")
    public Result delete(@RequestParam Long specialReportId) {
        specialReportService.delete(specialReportId);
        return Result.success();
    }
}
