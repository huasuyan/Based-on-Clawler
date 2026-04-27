package com.crawler.service.impl;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.AlertMessage;
import com.crawler.entity.dto.AlertMessage.AlertMessagePageQueryDto;
import com.crawler.entity.dto.AlertMessage.BatchMarkReadDto;
import com.crawler.mapper.AlertMessageMapper;
import com.crawler.service.AlertMessageService;
import com.crawler.websockets.VueSocketServer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AlertMessageServiceImpl implements AlertMessageService {

    @Resource
    private AlertMessageMapper alertMessageMapper;

    @Override
    public void saveAndPush(Long userId, Long alertId, String alertName, String content) {
        // 1. 持久化消息到数据库
        AlertMessage message = new AlertMessage();
        message.setUserId(userId);
        message.setAlertId(alertId);
        message.setAlertName(alertName);
        message.setContent(content);
        alertMessageMapper.insert(message);

        // 2. 推送 WebSocket 通知前端（type=alert_message，前端按此 type 弹窗）
        Map<String, Object> wsMsg = new HashMap<>();
        wsMsg.put("type",       "alert_message");
        wsMsg.put("user_id",    userId);
        wsMsg.put("alert_id",   alertId);
        wsMsg.put("alert_name", alertName);
        wsMsg.put("content",    content);
        wsMsg.put("message_id", message.getMessageId());

        VueSocketServer.sendToVue(userId.toString(), JSONUtil.toJsonStr(wsMsg));
        log.info("[消息通知] 已推送并持久化，userId={}, alertId={}", userId, alertId);
    }

    @Override
    public Map<String, Object> getUnreadCount(Long userId) {
        int count = alertMessageMapper.countUnread(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    @Override
    public Map<String, Object> pageList(Long userId, AlertMessagePageQueryDto queryDto) {
        boolean onlyUnread = Boolean.TRUE.equals(queryDto.getOnlyUnread());
        List<AlertMessage> list = alertMessageMapper.pageList(
                userId, onlyUnread, queryDto.getOffset(), queryDto.getPageSize());
        int total = alertMessageMapper.countPageList(userId, onlyUnread);

        Map<String, Object> result = new HashMap<>();
        result.put("total",    total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("list",     list);
        return result;
    }

    @Override
    public void markRead(Long userId, Long messageId) {
        // WHERE 子句包含 userId，防止越权操作
        alertMessageMapper.markRead(messageId, userId);
    }

    @Override
    public void batchMarkRead(Long userId, BatchMarkReadDto dto) {
        if (dto.getMessageIds() == null || dto.getMessageIds().isEmpty()) {
            throw new RuntimeException("消息ID列表不能为空");
        }
        alertMessageMapper.batchMarkRead(dto.getMessageIds(), userId);
    }

    @Override
    public void markAllRead(Long userId) {
        alertMessageMapper.markAllRead(userId);
    }
}