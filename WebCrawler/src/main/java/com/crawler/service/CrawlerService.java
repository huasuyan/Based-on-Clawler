package com.crawler.service;

import com.crawler.entity.Result;
import com.crawler.entity.dto.CrawlerUpdateDto;
import com.crawler.entity.dto.CrawlerPageQueryDTO;
import com.crawler.entity.dto.CrawlerDto;
import java.util.List;


public interface CrawlerService {

    List<CrawlerDto> pageList(CrawlerPageQueryDTO queryDTO);

    CrawlerDto getJobInfo(Integer string);

    Result updateCrawler(CrawlerUpdateDto crawlerUpdateDto);

    Result executeCrawler(Integer string);

    Result activateCrawler(Integer string);

    Result deleteCrawler(Integer string);
}
