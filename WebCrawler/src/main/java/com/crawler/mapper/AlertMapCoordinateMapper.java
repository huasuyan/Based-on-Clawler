package com.crawler.mapper;

import com.crawler.entity.AlertMapCoordinate;
import com.crawler.entity.dto.alertMap.AlertMapDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertMapCoordinateMapper {

    // 查询所有点位
    List<AlertMapDto> selectAll();

    // 根据预警ID查询
    List<AlertMapDto> selectByAlertId(Long alertId);

    // 查询今日预警
    List<AlertMapDto> selectAlertMapByDay(@Param("dayType") String dayType);
}