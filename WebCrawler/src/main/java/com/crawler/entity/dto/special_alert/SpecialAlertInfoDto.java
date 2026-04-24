package com.crawler.entity.dto.special_alert;

import lombok.Data;

@Data
public class SpecialAlertInfoDto {
    private Integer alertId;
    private Integer pageNum;
    private Integer pageSize;
    /**
     * 供 MyBatis XML 使用，自动计算 OFFSET
     * XML中写: limit #{pageSize} offset #{offset}
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
