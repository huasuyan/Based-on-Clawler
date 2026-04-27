package com.crawler.mapper;

import com.crawler.entity.AlertMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertMessageMapper {

    void insert(AlertMessage message);

    // 查询未读数量
    int countUnread(@Param("userId") Long userId);

    // 分页查询
    List<AlertMessage> pageList(
            @Param("userId")     Long    userId,
            @Param("onlyUnread") boolean onlyUnread,
            @Param("offset")     int     offset,
            @Param("pageSize")   int     pageSize
    );

    // 统计总条数（分页用）
    int countPageList(
            @Param("userId")     Long    userId,
            @Param("onlyUnread") boolean onlyUnread
    );

    // 标记单条已读（校验 userId 防越权）
    void markRead(@Param("messageId") Long messageId,
                  @Param("userId")    Long userId);

    // 批量标记已读
    void batchMarkRead(@Param("messageIds") List<Long> messageIds,
                       @Param("userId")     Long        userId);

    // 全部标记已读
    void markAllRead(@Param("userId") Long userId);
}