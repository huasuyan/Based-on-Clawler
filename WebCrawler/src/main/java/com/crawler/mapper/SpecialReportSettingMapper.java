package com.crawler.mapper;

import com.crawler.entity.SpecialReportSetting;
import com.crawler.entity.dto.SpecialReportEditDto;
import com.crawler.entity.dto.SpecialReportPageQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface SpecialReportSettingMapper {

    // 新增
    void insert(SpecialReportSetting setting);

    // 根据ID查询
    SpecialReportSetting selectById(Long specialReportId);

    // 分页查询
    List<SpecialReportSetting> pageList(SpecialReportPageQueryDto queryDto);

    // 编辑（动态更新）
    void update(SpecialReportEditDto editDto);

    // 更新启用状态
    void updateStatusEnabled(@Param("specialReportId") Long specialReportId,
                             @Param("statusEnabled") Integer statusEnabled);

    // 更新执行状态
    void updateExecuteStatus(@Param("specialReportId") Long specialReportId,
                             @Param("executeStatus") Integer executeStatus);

    // 更新 lastExecuteTime
    void updateLastExecuteTime(@Param("specialReportId") Long specialReportId,
                               @Param("lastExecuteTime") Date lastExecuteTime);

    // 删除（须处于停用状态）
    void deleteById(Long specialReportId);

    // 查询所有启用中的专题（调度器使用）
    List<SpecialReportSetting> selectAllEnabled();

    void updateLastUpdateTime(@Param("specialReportId") Long specialReportId,
                               @Param("lastUpdateTime") Date lastUpdateTime);

}
