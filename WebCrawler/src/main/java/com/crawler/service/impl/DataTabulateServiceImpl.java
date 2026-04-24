package com.crawler.service.impl;

import com.crawler.entity.AlertTabulate;
import com.crawler.entity.dto.ClearNewsData;
import com.crawler.entity.dto.SpecialAlertInfoDto;
import com.crawler.mapper.NewsDataMapper;
import com.crawler.service.DataTabulateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataTabulateServiceImpl implements DataTabulateService {

    @Resource
    private NewsDataMapper newsDataMapper;

    @Override
    public AlertTabulate countSpecialAlert(Integer alertId) {

        List<ClearNewsData> allData = getAllNewsData(alertId);




        return null;
    }

    private List<ClearNewsData> getAllNewsData(Integer alertId) {
        List<ClearNewsData> allData = new ArrayList<>();

        // 固定每页大小（可改大一点，更快）
        int pageSize = 100;
        int pageNum = 1;

        while (true) {
            SpecialAlertInfoDto dto = new SpecialAlertInfoDto();
            dto.setAlertId(alertId);
            dto.setPageNum(pageNum);
            dto.setPageSize(pageSize);

            // 查询一页
            List<ClearNewsData> pageList = newsDataMapper.infoList(dto);

            // 没有数据了，退出循环
            if (pageList == null || pageList.isEmpty()) {
                break;
            }

            // 加入总集合
            allData.addAll(pageList);

            // 下一页
            pageNum++;
        }

        return allData;
    }
}
