package com.crawler.service;

import com.crawler.entity.dto.AlertMessage.AlertMessagePageQueryDto;
import com.crawler.entity.dto.AlertMessage.BatchMarkReadDto;

import java.util.Map;

public interface AlertMessageService {

    // 保存消息并推送WebSocket（由 AlertUtil 调用）
    void saveAndPush(Long userId, Long alertId, String alertName, String content);

    // 查询未读数量
    Map<String, Object> getUnreadCount(Long userId);

    // 分页查询消息列表
    Map<String, Object> pageList(Long userId, AlertMessagePageQueryDto queryDto);

    // 标记单条已读
    void markRead(Long userId, Long messageId);

    // 批量标记已读
    void batchMarkRead(Long userId, BatchMarkReadDto dto);

    // 全部标记已读
    void markAllRead(Long userId);
}