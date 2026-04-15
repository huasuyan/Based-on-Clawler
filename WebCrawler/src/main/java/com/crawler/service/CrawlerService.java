package com.crawler.service;

import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.dto.CrawlerDto;
import java.util.List;


public interface CrawlerService {

    List<CrawlerDto> pageList(CrawlerPageQueryDTO queryDTO);

    CrawlerDto getJobInfo(String string);
}
