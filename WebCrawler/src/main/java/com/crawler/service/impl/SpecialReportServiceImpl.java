package com.crawler.service.impl;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialReportSetting;
import com.crawler.entity.dto.SpecialReportCreateDto;
import com.crawler.entity.dto.SpecialReportDto;
import com.crawler.entity.dto.SpecialReportEditDto;
import com.crawler.entity.dto.SpecialReportPageQueryDto;
import com.crawler.mapper.SpecialReportSettingMapper;
import com.crawler.service.SpecialReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpecialReportServiceImpl implements SpecialReportService {

    @Resource
    private SpecialReportSettingMapper specialReportSettingMapper;

    @Override
    public Map<String, Object> create(SpecialReportCreateDto createDto) {
        // 参数校验
        if (createDto.getReportName() == null || createDto.getReportName().isBlank()) {
            throw new RuntimeException("报告名称不能为空");
        }
        if (createDto.getMonitorKeywords() == null || createDto.getMonitorKeywords().isBlank()) {
            throw new RuntimeException("监测词组不能为空");
        }
        if (createDto.getReportType() == null) {
            throw new RuntimeException("报告类型不能为空");
        }
        if (createDto.getTypeParams() == null || createDto.getTypeParams().isBlank()) {
            throw new RuntimeException("类型参数不能为空");
        }

        // 校验 typeParams 格式，并设置 lastExecuteTime
        Date lastExecuteTime = resolveLastExecuteTime(createDto);

        SpecialReportSetting setting = new SpecialReportSetting();
        setting.setReportName(createDto.getReportName());
        setting.setCreateUserId(createDto.getCreateUserId());
        setting.setMonitorKeywords(createDto.getMonitorKeywords());
        setting.setDataSource(createDto.getDataSource());
        setting.setParams(createDto.getParams());
        setting.setMonitorRegion(createDto.getMonitorRegion());
        // 默认启用
        setting.setStatusEnabled(
                createDto.getStatusEnabled() != null ? createDto.getStatusEnabled() : 1);
        setting.setReportType(createDto.getReportType());
        setting.setTypeParams(createDto.getTypeParams());
        setting.setLastExecuteTime(lastExecuteTime);
        setting.setLastUpdateTime(new Date());

        specialReportSettingMapper.insert(setting);

        Map<String, Object> result = new HashMap<>();
        result.put("specialReportId", setting.getSpecialReportId());
        return result;
    }

    @Override
    public void edit(SpecialReportEditDto editDto) {
        SpecialReportSetting existing =
                specialReportSettingMapper.selectById(editDto.getSpecialReportId());
        if (existing == null) {
            throw new RuntimeException("报告专题不存在");
        }
        specialReportSettingMapper.update(editDto);
    }

    @Override
    public Map<String, Object> pageList(SpecialReportPageQueryDto queryDto) {
        List<SpecialReportDto> list = specialReportSettingMapper.pageList(queryDto)
                .stream()
                .map(SpecialReportDto::new)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("reportList", list);
        return result;
    }

    @Override
    public void delete(Long specialReportId) {
        SpecialReportSetting existing =
                specialReportSettingMapper.selectById(specialReportId);
        if (existing == null) {
            throw new RuntimeException("报告专题不存在");
        }
        if (existing.getStatusEnabled() == 1) {
            throw new RuntimeException("请先停用报告专题后再删除");
        }
        specialReportSettingMapper.deleteById(specialReportId);
    }

    /**
     * 根据报告类型和 typeParams 推导初始 lastExecuteTime：
     * - 即时报告：初始设置为 start_date 当天的 00:00:00（保证当天可以触发）
     * - 定时报告：初始设置为当前时间（保证首次判断正确）
     */
    private Date resolveLastExecuteTime(SpecialReportCreateDto createDto) {
        try {
            if (createDto.getReportType() == 1) {
                // 即时报告：取 start_date，转为 Date，时间部分设为 00:00:00 前一秒
                // 这样调度器扫描到当天时，lastExecuteTime 不是今天，会触发
                Map<String, Object> tp = JSONUtil.parseObj(createDto.getTypeParams());
                String startDateStr = (String) tp.get("start_date");
                if (startDateStr == null) throw new RuntimeException("即时报告缺少 start_date");
                LocalDate startDate = LocalDate.parse(startDateStr);
                // 设为开始日期前一天，确保开始当天可以触发
                return java.sql.Date.valueOf(startDate.minusDays(1));
            } else {
                // 定时报告：初始为当前时间
                return new Date();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("typeParams 格式错误：" + e.getMessage());
        }
    }
}
