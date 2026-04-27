package com.crawler.service.impl;

import com.crawler.entity.Cases;
import com.crawler.entity.CaseText;
import com.crawler.entity.dto.case_management.*;
import com.crawler.mapper.CaseManagementMapper;
import com.crawler.service.CaseManagementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CaseManagementServiceImpl implements CaseManagementService {

    @Resource
    private CaseManagementMapper caseManagementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(CaseCreateDto createDto, Long userId) {
        Cases cm = new Cases();
        cm.setCaseName(createDto.getCaseName());
        cm.setCaseInfo(createDto.getCaseInfo());
        cm.setCaseLevel(createDto.getCaseLevel());
        cm.setMoney(createDto.getMoney());
        cm.setNewsId(createDto.getNewsId());
        // triggerState 默认 1（已启用），state 默认 0（待办）
        caseManagementMapper.insert(cm);

        Map<String, Object> result = new HashMap<>();
        result.put("caseId", cm.getCaseId());
        return result;
    }

    @Override
    public Map<String, Object> pageList(CasePageQueryDto queryDto) {
        List<CaseListDto> list = caseManagementMapper.pageList(queryDto)
                .stream().map(CaseListDto::new).collect(Collectors.toList());
        int total = caseManagementMapper.countPageList(queryDto);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pageNum", queryDto.getPageNum());
        result.put("pageSize", queryDto.getPageSize());
        result.put("caseList", list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitText(CaseSubmitTextDto submitDto, Long userId) {
        Cases existing = caseManagementMapper.selectById(submitDto.getCaseId());
        if (existing == null) {
            throw new RuntimeException("办件不存在");
        }

        CaseText text = new CaseText();
        text.setCaseId(submitDto.getCaseId());
        text.setUserId(userId);
        text.setType(submitDto.getType());
        text.setContent(submitDto.getContent());
        caseManagementMapper.insertText(text);

        Map<String, Object> result = new HashMap<>();
        result.put("textId", text.getTextId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> process(Long caseId) {
        Cases cm = caseManagementMapper.selectById(caseId);
        if (cm == null) throw new RuntimeException("办件不存在");

        // 校验是否已填写办理意见（type=0）
        int count = caseManagementMapper.countTextByType(caseId, 0);
        if (count == 0) {
            throw new RuntimeException("请先填写办理意见");
        }

        caseManagementMapper.updateState(caseId, 1);
        Map<String, Object> result = new HashMap<>();
        result.put("state", 1);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> archive(Long caseId) {
        Cases cm = caseManagementMapper.selectById(caseId);
        if (cm == null) throw new RuntimeException("办件不存在");
        if (cm.getState() != 1) {
            throw new RuntimeException("办件须处于'办理中'状态才能归档");
        }

        int count = caseManagementMapper.countTextByType(caseId, 1);
        if (count == 0) {
            throw new RuntimeException("请先填写归档意见");
        }

        caseManagementMapper.updateState(caseId, 2);
        Map<String, Object> result = new HashMap<>();
        result.put("state", 2);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> close(Long caseId) {
        Cases cm = caseManagementMapper.selectById(caseId);
        if (cm == null) throw new RuntimeException("办件不存在");
        if (cm.getTriggerState() == 0) {
            throw new RuntimeException("办件已处于关闭状态");
        }

        int count = caseManagementMapper.countTextByType(caseId, 3);
        if (count == 0) {
            throw new RuntimeException("请先填写停用说明");
        }

        caseManagementMapper.updateTriggerState(caseId, 0);
        Map<String, Object> result = new HashMap<>();
        result.put("triggerState", 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> enable(Long caseId) {
        Cases cm = caseManagementMapper.selectById(caseId);
        if (cm == null) throw new RuntimeException("办件不存在");
        if (cm.getTriggerState() == 1) {
            throw new RuntimeException("办件已处于启用状态");
        }

        int count = caseManagementMapper.countTextByType(caseId, 2);
        if (count == 0) {
            throw new RuntimeException("请先填写启用说明");
        }

        caseManagementMapper.updateTriggerState(caseId, 1);
        Map<String, Object> result = new HashMap<>();
        result.put("triggerState", 1);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> markException(Long caseId) {
        Cases cm = caseManagementMapper.selectById(caseId);
        if (cm == null) throw new RuntimeException("办件不存在");
        if (cm.getState() != 0 && cm.getState() != 1) {
            throw new RuntimeException("办件须处于'待办'或'办理中'状态才能标记异常");
        }

        int count = caseManagementMapper.countTextByType(caseId, 4);
        if (count == 0) {
            throw new RuntimeException("请先填写异常说明");
        }

        caseManagementMapper.updateState(caseId, 3);
        Map<String, Object> result = new HashMap<>();
        result.put("state", 3);
        return result;
    }

    @Override
    public Map<String, Object> textList(Long caseId) {
        if (caseId == null) throw new RuntimeException("办件ID不能为空");

        List<CaseText> list = caseManagementMapper.selectTextList(caseId);
        Map<String, Object> result = new HashMap<>();
        result.put("textList", list);
        return result;
    }
}
