package com.crawler.mapper;

import com.crawler.entity.Cases;
import com.crawler.entity.CaseText;
import com.crawler.entity.dto.case_management.CasePageQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CaseManagementMapper {

    // 新增办件
    void insert(Cases aCases);

    // 分页条件查询
    List<Cases> pageList(CasePageQueryDto queryDto);

    // 分页总数
    int countPageList(CasePageQueryDto queryDto);

    // 根据ID查询
    Cases selectById(@Param("caseId") Long caseId);

    // 更新办件状态
    void updateState(@Param("caseId") Long caseId, @Param("state") Integer state);

    // 更新启用/关闭状态
    void updateTriggerState(@Param("caseId") Long caseId, @Param("triggerState") Integer triggerState);

    // 更新办件（含 updateTime）
    void update(Cases aCases);

    // ========== 办件上传信息 ==========

    // 新增上传信息
    void insertText(CaseText text);

    // 查询某办件下的上传信息列表（按 createTime 升序）
    List<CaseText> selectTextList(@Param("caseId") Long caseId);

    // 统计某办件某类型上传信息数量
    int countTextByType(@Param("caseId") Long caseId, @Param("type") Integer type);
}
