package com.crawler.entity.dto.dashboard;

import lombok.Data;

@Data
public class RegionRankDto {
    private Integer rank;
    private String region;
    private Integer newsCount;
}