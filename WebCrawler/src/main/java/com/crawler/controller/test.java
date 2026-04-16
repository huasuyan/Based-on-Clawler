package com.crawler.controller;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.xxljob.XxlJobInfo;
import com.crawler.util.XxlJobUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/test")
public class test {
    @Resource
    private XxlJobUtil xxlJobUtil;

    @RequestMapping("/get")
    public Object cc(){
        //调用格式
        Map<String,Object> map = new HashMap<>();//用Map传入GET需要的参数
        map.put("offset","");
        map.put("pagesize","");       // 注意哪些是必要的参数，哪些是可选的
        map.put("jobGroup", 10);
        map.put("triggerStatus", -1);
        map.put("jobDesc", "");
        map.put("executorHandler", "");
        map.put("author", "");
        Object data = xxlJobUtil.doGet("/jobinfo/pageList", map);//path写接口路径
        List<XxlJobInfo> list = JSONUtil.toList(JSONUtil.parseArray(data), XxlJobInfo.class);//需要自己新建一个对应返回的类
        return list;
    }

    @RequestMapping("/post")
    public Object post(){
        Map<String, Object> map = new HashMap<>();
        map.put("jobGroup", 10);
        map.put("jobDesc", "测试insert");
        map.put("author", "huasuyan");
        // 调度类型，必填
        map.put("scheduleType", "NONE");
        map.put("scheduleConf", "");
        // 过期策略，必填
        map.put("misfireStrategy", "DO_NOTHING");
        // 执行器相关，必填
        map.put("executorRouteStrategy", "FIRST");
        map.put("executorHandler", "demoJobHandler");
        map.put("executorParam", "");
        map.put("executorBlockStrategy", "SERIAL_EXECUTION");
        map.put("executorTimeout", 0);
        map.put("executorFailRetryCount", 0);
        // GLUE类型，必填
        map.put("glueType", "BEAN");
        map.put("glueRemark", "");
        // 其他选填
        map.put("alarmEmail", "");
        map.put("childJobId", "");

        Object data = xxlJobUtil.doPostForm("/jobinfo/insert", map);
        return data;
    }
}
