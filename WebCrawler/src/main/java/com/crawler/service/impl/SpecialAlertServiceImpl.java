package com.crawler.service.impl;

import com.crawler.entity.NewsData;
import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.Result;
import com.crawler.entity.dto.*;
import com.crawler.entity.dto.special_alert.*;
import com.crawler.mapper.SpecialAlertSettingMapper;
import com.crawler.mapper.NewsDataMapper;
import com.crawler.mapper.UserMapper;
import com.crawler.service.SpecialAlertService;
import com.crawler.util.PythonCronAsync;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class SpecialAlertServiceImpl implements SpecialAlertService {

    @Resource
    private SpecialAlertSettingMapper specialAlertSettingMapper;

    @Resource
    private NewsDataMapper newsDataMapper;

    @Resource
    private PythonCronAsync pythonCronAsync;

    @Resource
    private UserMapper userMapper;

    // 列表查询
    @Override
    public Map<String, Object> pageList(SpecialAlertPageQueryDto queryDto) {
        Map<String, Object> result = new HashMap<>();
        List<SpecialAlertDto> alertInfo = specialAlertSettingMapper.pageList(queryDto)
                .stream()
                .map(SpecialAlertDto::new)
                .collect(Collectors.toList());
        int total = specialAlertSettingMapper.countPageList(queryDto);

        // 给列表每一项设置 userName（当前登录用户名称）
        if (alertInfo != null && !alertInfo.isEmpty()) {
            for (SpecialAlertDto dto : alertInfo) {
                dto.setUserName(userMapper.selectUserName(dto.getUserId()));
            }
        }

        result.put("total",    total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("alertInfo", alertInfo);

        return result;
    }

    @Override
    public SpecialAlertSetting getSpecialAlertById(Long alertId) {
        SpecialAlertSetting alertInfo = specialAlertSettingMapper.selectByAlertId(alertId);
        return alertInfo;
    }

    // 新增
    @Override
    public Map<String, Object> create(SpecialAlertCreateDto createDto) {
        SpecialAlertSetting specialAlertSetting = new SpecialAlertSetting();
        specialAlertSetting.setUserId(createDto.getUserId());
        specialAlertSetting.setAlertName(createDto.getAlertName());
        specialAlertSetting.setTargetSource(createDto.getTargetSource());
        specialAlertSetting.setKeyWord(createDto.getKeyWord());
        specialAlertSetting.setParams(createDto.getParams());
        specialAlertSetting.setFrequency(createDto.getFrequency());
        specialAlertSetting.setAlertTrigger(createDto.getAlertTrigger());
        specialAlertSetting.setTimeRange(createDto.getTimeRange());
        specialAlertSetting.setAlertMethod(createDto.getAlertMethod());
        specialAlertSetting.setDedupEnable(createDto.getDedupEnable());
        specialAlertSetting.setAlertLevel(createDto.getAlertLevel());

        specialAlertSettingMapper.insert(specialAlertSetting);  // useGeneratedKeys，alertId 回填

        Map<String, Object> result = new HashMap<>();
        result.put("alertId", specialAlertSetting.getAlertId());
        return result;
    }

    // 编辑（专题须处于关闭状态）
    @Override
    public Map<String, Object> edit(SpecialAlertEditDto editDto) {
        SpecialAlertSetting existing = specialAlertSettingMapper.selectByAlertId(editDto.getAlertId());
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }
        if (existing.getTriggerState() == 1) {
            throw new RuntimeException("请先关闭预警专题后再编辑");
        }

        SpecialAlertSetting update = new SpecialAlertSetting();
        update.setAlertId(editDto.getAlertId());
        update.setAlertName(editDto.getAlertName());
        update.setTargetSource(editDto.getTargetSource());
        update.setKeyWord(editDto.getKeyWord());
        update.setParams(editDto.getParams());
        update.setFrequency(editDto.getFrequency());
        update.setAlertTrigger(editDto.getAlertTrigger());
        update.setTimeRange(editDto.getTimeRange());
        update.setAlertMethod(editDto.getAlertMethod());
        update.setDedupEnable(editDto.getDedupEnable());
        update.setAlertLevel(editDto.getAlertLevel());

        specialAlertSettingMapper.update(update);

        Map<String, Object> result = new HashMap<>();
        result.put("alertId", editDto.getAlertId());
        return result;
    }

    // ----------------------------------------------------------------
    //  启用 / 关闭（异步 HTTP 调用 Python）
    // ----------------------------------------------------------------

    @Override
    public Map<String, Object> toggleTriggerState(Long alertId) {
        SpecialAlertSetting existing = specialAlertSettingMapper.selectByAlertId(alertId);
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }

        Map<String, Object> result = new HashMap<>();
        if (existing.getTriggerState() == 0) {
            // ── 当前关闭 → 启用 ──────────────────────────────────────
            // 1. 更新 DB，主线程立即返回前端
            specialAlertSettingMapper.updateTriggerState(alertId, 1);
            // 2. 开启进程2：异步 HTTP 调用 Python，等待结果后写文件
            pythonCronAsync.callPythonAsync(existing);
            result.put("triggerState", 1);
        } else {
            // ── 当前启用 → 关闭 ──────────────────────────────────────
            // 只更新 DB，Python 侧定时任务自然停止（不再被调用）
            specialAlertSettingMapper.updateTriggerState(alertId, 0);
            specialAlertSettingMapper.updateState(alertId, 0);
            result.put("triggerState", 0);
        }
        return result;
    }

    // 删除专题（须处于关闭状态）
    @Override
    public Result delete(Long alertId) {
        SpecialAlertSetting existing = specialAlertSettingMapper.selectByAlertId(alertId);
        if (existing == null) {
            throw new RuntimeException("预警专题不存在");
        }
        if (existing.getTriggerState() == 1) {
            throw new RuntimeException("请先关闭预警专题后再删除");
        }
        specialAlertSettingMapper.deleteByAlertId(alertId);
        newsDataMapper.deleteByAlertId(alertId);
        return Result.success();
    }

    // 舆情消息列表---待修改逻辑
    @Override
    public Map<String, Object> infoList(SpecialAlertInfoDto queryDto) {
        List<ClearNewsData> ClearNewsData = newsDataMapper.infoList(queryDto);
        Map<String, Object> result = new HashMap<>();
        int total = newsDataMapper.countPageList(queryDto);
        result.put("total", total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("NewsDataList", ClearNewsData);
        return result;
    }

    // 删除舆情消息
    @Override
    public Result infoDelete(Long newsId) {
        NewsData existing = newsDataMapper.select(newsId);
        if (existing == null) {
            throw new RuntimeException("舆情消息不存在");
        }
        newsDataMapper.delete(newsId);
        return Result.success();
    }

    @Override
    public Map<String, Object> searchAllAlert(List<Long> userIdList) {
        List<SpecialAlertListDto> res = specialAlertSettingMapper.searchAllAlert(userIdList);
        Map<String, Object> result = new HashMap<>();
        result.put("alertInfos", res);
        return result;
    }

}
