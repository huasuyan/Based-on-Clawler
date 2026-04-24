package com.crawler.service;

import com.crawler.entity.Result;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.dto.*;

import java.util.List;
import java.util.Map;

public interface SpecialAlertService {

    //显示预警专题列表（分页）
    List<SpecialAlertDto> pageList(SpecialAlertPageQueryDto queryDto);

    //查询预警专题
    SpecialAlertSetting getSpecialAlertById(Integer alertId);

    //新增预警专题
    Map<String, Object> create(SpecialAlertCreateDto createDto);

    //编辑预警专题（专题须处于关闭状态）
    Map<String, Object> edit(SpecialAlertEditDto editDto);

    //启用 / 关闭预警专题（异步，通知Python）
    Map<String, Object> toggleTriggerState(Integer alertId);

    //删除预警专题（专题须处于关闭状态）
    Result delete(Integer alertId);

    // -------------------- 预警信息（JSON文件） --------------------

    //显示某专题的舆情消息列表
    Map<String, Object> infoList(SpecialAlertInfoDto queryDto);

    //删除舆情消息
    Result infoDelete(Long newsId);
}
