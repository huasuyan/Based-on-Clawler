package com.crawler.mapper;

import com.crawler.entity.AlertTabulate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlertTabulateMapper {
    // 插入统计结果
    int insert(AlertTabulate tabulate);
}
