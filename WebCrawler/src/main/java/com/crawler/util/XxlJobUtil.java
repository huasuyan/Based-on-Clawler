package com.crawler.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
        log.info("xxl-job原始响应: {}", responseBody); // 打印原始响应
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


