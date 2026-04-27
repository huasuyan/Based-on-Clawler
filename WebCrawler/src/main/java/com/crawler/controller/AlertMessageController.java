package com.crawler.controller;

import com.crawler.entity.Result;
import com.crawler.entity.User;
import com.crawler.entity.dto.AlertMessage.AlertMessagePageQueryDto;
import com.crawler.entity.dto.AlertMessage.BatchMarkReadDto;
import com.crawler.service.AlertMessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alertMessage")
public class AlertMessageController {

    @Resource
    private AlertMessageService alertMessageService;

    /**
     * 获取未读消息数量（前端定时轮询）
     */
    @GetMapping("/unreadCount")
    public Result unreadCount(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        Map<String, Object> data = alertMessageService.getUnreadCount(currentUser.getUserId());
        return Result.success(data);
    }

    /**
     * 分页查询消息列表
     */
    @PostMapping("/pageList")
    public Result pageList(HttpServletRequest request,
                           @RequestBody AlertMessagePageQueryDto queryDto) {
        User currentUser = (User) request.getAttribute("currentUser");
        Map<String, Object> data = alertMessageService.pageList(currentUser.getUserId(), queryDto);
        return Result.success(data);
    }

    /**
     * 标记单条消息已读
     */
    @PostMapping("/markRead")
    public Result markRead(HttpServletRequest request,
                           @RequestBody Map<String, Long> body) {
        User currentUser = (User) request.getAttribute("currentUser");
        Long messageId = body.get("messageId");
        if (messageId == null) {
            return Result.error("messageId不能为空");
        }
        alertMessageService.markRead(currentUser.getUserId(), messageId);
        return Result.success();
    }

    /**
     * 批量标记消息已读
     */
    @PostMapping("/batchMarkRead")
    public Result batchMarkRead(HttpServletRequest request,
                                @RequestBody BatchMarkReadDto dto) {
        User currentUser = (User) request.getAttribute("currentUser");
        alertMessageService.batchMarkRead(currentUser.getUserId(), dto);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/markAllRead")
    public Result markAllRead(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        alertMessageService.markAllRead(currentUser.getUserId());
        return Result.success();
    }
}