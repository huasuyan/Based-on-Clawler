package com.crawler.mapper;

import com.crawler.entity.ReportResult;
import com.crawler.entity.dto.report.ReportResultEditDto;
import com.crawler.entity.dto.report.ReportResultPageQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportResultMapper {

    void insert(ReportResult reportResult);

    ReportResult selectById(Long reportId);

    List<ReportResult> pageList(ReportResultPageQueryDto queryDto);

    int countPageList(ReportResultPageQueryDto queryDto);

    void update(ReportResultEditDto editDto);

    void deleteById(Long reportId);

    List<ReportResult> selectBySpecialReportId(@Param("specialReportId") Long specialReportId);
}
