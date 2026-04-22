package com.crawler.service;

import com.crawler.entity.CrawlerNoneResult;
import com.crawler.entity.dto.CrawlerCreateDto;
import jakarta.servlet.http.HttpServletResponse;

public interface CrawlerNoneService {

    Integer createAndRun(CrawlerCreateDto crawler);

    CrawlerNoneResult getTaskResult(Integer crawlerId);

    void export(Integer crawlerId, String exportFormat, HttpServletResponse response);
}
