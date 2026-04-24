package com.crawler.mapper;

import com.crawler.entity.SpecialAlertSetting;
import com.crawler.entity.dto.special_alert.SpecialAlertListDto;
import com.crawler.entity.dto.special_alert.SpecialAlertPageQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 预警专题 Mapper
 */
@Mapper
public interface SpecialAlertSettingMapper {

    // 新增预警专题
    void insert(SpecialAlertSetting specialAlertSetting);

    // 根据alertId查询预警专题
    SpecialAlertSetting selectByAlertId(Integer alertId);

    // 分页列表查询（支持多条件筛选）
    List<SpecialAlertSetting> pageList(SpecialAlertPageQueryDto queryDto);

    // 更新专题信息（编辑接口使用）
    void update(SpecialAlertSetting specialAlertSetting);

    // 更新启用状态
    void updateTriggerState(@Param("alertId") Integer alertId,
                            @Param("triggerState") Integer triggerState);

    // 更新运行状态（Python回调使用）
    void updateState(@Param("alertId") Integer alertId,
                     @Param("state") Integer state);

    // 删除专题
    void deleteByAlertId(Integer alertId);

    List<SpecialAlertSetting> selectByTriggerState(@Param("triggerState") Integer triggerState);

    // 重置 pending_count
    void resetPendingCount(@Param("alertId") Integer alertId);

    // 累加 pending_count（每次爬取后调用）
    void addPendingCount(@Param("alertId") Integer alertId,
                         @Param("delta") int delta);

    void updateLastTriggerTime(@Param("alertId") Integer alertId);

    void updateLatestNewsTime(@Param("alertId") Integer alertId,
                              @Param("latestNewsTime") Date latestNewsTime);

    // 统计总条数（分页用）
    int countPageList(SpecialAlertPageQueryDto queryDto);

    List<SpecialAlertListDto> searchAllAlert(Integer userId);
}
