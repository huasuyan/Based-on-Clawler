package com.crawler.entity.dto.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardPlatformDto {
    private List<PlatformBarDto> barData;
    private List<PlatformPieDto> pieData;
}