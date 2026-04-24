package com.crawler.service.impl;

import cn.hutool.json.JSONObject;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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

        Integer reportType = createDto.getReportType();
        JSONObject tp = JSONUtil.parseObj(createDto.getTypeParams().toString());

        setting.setReportType(reportType);
        setting.setTypeParams(createDto.getTypeParams());
        if (reportType == 1) {
            String startDateStr = tp.getStr("start_date");
            if (StringUtils.isNotBlank(startDateStr)) {
                try {
                    LocalDate localDate = LocalDate.parse(startDateStr);
                    Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    setting.setLastUpdateTime(date);
                } catch (DateTimeParseException e) {
                    log.error("start_date 格式错误，应为 yyyy-MM-dd，实际值: {}", startDateStr, e);
                }
            }
        }
        if(reportType == 2){
            setting.setLastUpdateTime(new Date());
        }
        setting.setLastExecuteTime(new Date());


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

        int total = specialReportSettingMapper.countPageList(queryDto);
        result.put("total",    total);
        result.put("pageNum",  queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
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

}
