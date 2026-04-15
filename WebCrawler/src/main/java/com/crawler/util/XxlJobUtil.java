package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.crawler.entity.Crawler;
import com.crawler.entity.dto.CrawlerDto;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.mapper.CrawlerMapper;
import com.crawler.mapper.XxlJobInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class XxlJobUtil {
    //注入RedisTemplate
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.admin.username}")
    private String username;

    @Value("${xxl.job.admin.password}")
    private String password;

    @Resource
    private XxlJobInfoMapper xxlJobInfoMapper;

    @Resource
    private CrawlerMapper crawlerMapper;

    public void login() {
        String url=adminAddresses+"/auth/doLogin";
        HttpResponse response = HttpRequest.post(url)
                .form("userName",username)
                .form("password",password)
                .execute();
        List<HttpCookie> cookies = response.getCookies();
        Optional<HttpCookie> cookieOpt = cookies.stream()
                .filter(cookie -> cookie.getName().equals("xxl_job_login_token")).findFirst();
        if (!cookieOpt.isPresent())
            throw new RuntimeException("get xxl-job cookie error!");

        String value = cookieOpt.get().getValue();
        redisTemplate.opsForValue().set("xxl_job_login_token", value);
    }

    public CrawlerDto getJobInfo(String jobId) {
        XxlJobInfo jobInfo = xxlJobInfoMapper.selectByJobId(Integer.parseInt(jobId));
        Crawler crawler = crawlerMapper.selectByCrawlerId(jobId);
        CrawlerDto crawlerDto = new CrawlerDto();
        crawlerDto.setCrawlerName(crawler.getCrawlerName());
        crawlerDto.setCrawlerId(Integer.parseInt(jobId));
        crawlerDto.setConfigMethod(crawler.getConfigMethod());
        crawlerDto.setScheduleConf(jobInfo.getScheduleConf());
        crawlerDto.setScheduleType(jobInfo.getScheduleType());
        if(jobInfo.getTriggerLastTime() != 0){
            // 上次执行时间不为0，说明任务已执行过,则设置上次执行时间
            crawlerDto.setTriggerLastTime(new Date(jobInfo.getTriggerLastTime()));
        }else{
            // 上次执行时间为0，说明任务未执行过,则设置上次执行时间为null
            crawlerDto.setTriggerLastTime(null);
        }
        if(jobInfo.getTriggerNextTime() != 0){
            // 下次执行时间不为0，说明任务已执行过,则设置下次执行时间
            crawlerDto.setTriggerNextTime(new Date(jobInfo.getTriggerNextTime()));
        }else{
            // 下次执行时间为0，说明任务未执行过,则设置下次执行时间为null
            crawlerDto.setTriggerNextTime(null);
        }
        crawlerDto.setJobDesc(jobInfo.getJobDesc());
        crawlerDto.setUpdateTime(jobInfo.getUpdateTime());
        crawlerDto.setTriggerStatus(jobInfo.getTriggerStatus());
        return crawlerDto;
    }

}
