package com.crawler.entity.dto.AlertMessage;

import lombok.Data;

@Data
public class AlertMessagePageQueryDto {
    private Integer pageNum    = 1;
    private Integer pageSize   = 10;
    private Boolean onlyUnread = false;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}