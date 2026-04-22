package com.crawler.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportDto {
    private Integer crawlerId;      // 任务ID
    private String exportFormat;   // 导出格式：excel/csv/json
}
