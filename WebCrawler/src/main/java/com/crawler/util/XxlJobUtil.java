package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
import java.util.Map;
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
        log.debug(value);
        redisTemplate.opsForValue().set("xxl_job_login_token", value);
    }

    /**
     * 从redis中获取cookie
     * 如果redis中没有cookie，则登录获取cookie
     * 循环3次获取不到cookie，则抛出异常
     * @return
     */
    public String getCookie() {
        for (int i = 0; i < 3; i++) {
            String cookieStr = redisTemplate.opsForValue().get("xxl_job_login_token").toString();
            if (cookieStr !=null) {
                return "xxl_job_login_token="+cookieStr;
            }
            login();
        }
        throw new RuntimeException("get xxl-job cookie error!");
    }

    // 统一提取 data 字段的私有方法
    private Object extractData(String responseBody) {
        if (!JSONUtil.isTypeJSON(responseBody)) {
            throw new RuntimeException("响应体非JSON格式: " + responseBody);
        }
        JSONObject json = JSONUtil.parseObj(responseBody);
        Integer code = json.getInt("code");
        if (code == null || code != 200) {
            throw new RuntimeException("请求失败: " + json.getStr("msg"));
        }
        Object data = json.get("data");
        if (data == null) return null;

        // 如果 data 里还有 data 字段（分页结构），继续取内层
        JSONObject dataObj = JSONUtil.parseObj(data.toString());
        if (dataObj.containsKey("data")) {
            return dataObj.get("data");
        }
        return data;
    }

    // -----------------------------------------------
// GET：支持请求头、Cookie、路径参数（Query String）
// -----------------------------------------------
    public Object doGet(String path, Map<String, Object> queryParams) {
        String cookie = getCookie();
        HttpRequest request = HttpRequest.get(adminAddresses + path)
                .header("Cookie", cookie)
                .header("Content-Type", "application/json");
        // 添加路径参数（Query String）
        if (queryParams != null) {
            queryParams.forEach(request::form);
        }
        try (HttpResponse response = request.execute()) {
            return extractData(response.body());
        }
    }

    // -----------------------------------------------
// POST JSON：支持请求头、Cookie、JSON body
// -----------------------------------------------
    public Object doPostJson(String path, Object bodyObj) {
        String cookie = getCookie();
        String jsonBody = bodyObj != null ? JSONUtil.toJsonStr(bodyObj) : "";
        try (HttpResponse response = HttpRequest.post(adminAddresses + path)
                .header("Cookie", cookie)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .execute()) {
            return extractData(response.body());
        }
    }

}


