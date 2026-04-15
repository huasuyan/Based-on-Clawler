package com.crawler.mapper;

import com.crawler.entity.xxljob.XxlJobInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface XxlJobInfoMapper {
    XxlJobInfo selectByJobId(Integer jobId);
}
