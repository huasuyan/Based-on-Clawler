package com.crawler.entity.dto.dashboard;

import lombok.Data;

@Data
public class PlatformBarDto {
    private String source;
    private Integer sensitive;
    private Integer neutral;
    private Integer normal;
    private Integer total;

}