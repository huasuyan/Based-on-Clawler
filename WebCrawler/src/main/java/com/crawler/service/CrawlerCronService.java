package com.crawler.service;

import com.crawler.entity.CrawlerCron;
import com.crawler.entity.Result;
import com.crawler.entity.dto.*;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface CrawlerCronService {

    //显示预警专题列表（分页）
    Map<String, Object> pageList(CrawlerCronPageQueryDto queryDto);

    //新增预警专题
    Map<String, Object> create(CrawlerCronCreateDto createDto);

    //编辑预警专题（专题须处于关闭状态）
    Map<String, Object> edit(CrawlerCronEditDto editDto);

    //启用 / 关闭预警专题（异步，通知Python）
    Map<String, Object> toggleTriggerState(Integer crawlerId);

    //删除预警专题（专题须处于关闭状态）
    Result delete(Integer crawlerId);

    // -------------------- 预警信息（JSON文件） --------------------

    //显示某专题的预警信息文件列表
    Map<String, Object> infoList(Integer crawlerId, Integer pageNum, Integer pageSize);

    //显示预警信息的详细内容
    Map<String, Object> info(Integer crawlerId, String infoFileName);

    //删除预警信息文件
    Result infoDelete(Integer crawlerId, String infoFileName);
}
