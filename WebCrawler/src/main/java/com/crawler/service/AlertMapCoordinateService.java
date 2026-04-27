package com.crawler.service;

import com.crawler.entity.dto.alertMap.AlertMapDto;

import java.util.List;

public interface AlertMapCoordinateService {
    List<AlertMapDto> getAll();
    List<AlertMapDto> getByAlertId(Long alertId);
    List<AlertMapDto> getAlertMap(String dayType);
}