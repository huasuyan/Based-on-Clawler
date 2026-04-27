package com.crawler.service.impl;

import com.crawler.entity.AlertMapCoordinate;
import com.crawler.entity.dto.alertMap.AlertMapDto;
import com.crawler.mapper.AlertMapCoordinateMapper;
import com.crawler.service.AlertMapCoordinateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertMapCoordinateServiceImpl implements AlertMapCoordinateService {

    @Resource
    private AlertMapCoordinateMapper alertMapCoordinateMapper;

    @Override
    public List<AlertMapDto> getAll() {
        return alertMapCoordinateMapper.selectAll();
    }

    @Override
    public List<AlertMapDto> getByAlertId(Long alertId) {
        return alertMapCoordinateMapper.selectByAlertId(alertId);
    }

    @Override
    public List<AlertMapDto> getAlertMap(String dayType) {
        return alertMapCoordinateMapper.selectAlertMapByDay(dayType);
    }
}
