
package com.crawler.util;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 短信工具类
 * 用于生成和验证短信验证码
 */
@Component
public class SmsUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 阿里云短信API配置
    @Value("${sms.aliyun.host:https://gyytz.market.alicloudapi.com}")
    private String host;

    @Value("${sms.aliyun.path:sms/smsSend}")
    private String path;

    @Value("${sms.aliyun.method:POST}")
    private String method;

    @Value("${sms.aliyun.appcode:f73828f54e754318ab549e18357e713c}")
    private String appcode;

    @Value("${sms.aliyun.smsSignId:2e65b1bb3d054466b82f0c9d125465e2}")
    private String smsSignId;

    @Value("${sms.aliyun.templateId:908e94ccf08b4476ba6c876d13f084ad}")
    private String templateId;

    /**
     * 生成短信验证码
     * @return 6位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送短信验证码
     * @param phone 手机号码
     * @param code 验证码
     */

    public void sendSms(String phone, String code) {
        if (appcode == null || appcode.isEmpty()) {
            throw new IllegalArgumentException("appcode is null");
        }

        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);

        Map<String, String> querys = new HashMap<>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:" + code + ",**minute**:5");
        querys.put("smsSignId", smsSignId);
        querys.put("templateId", templateId);

        Map<String, String> bodys = new HashMap<>();

        try {
            HttpResponse response = HttpUtil.doPost(host, path, method, headers, querys, bodys);
            System.out.println("短信发送响应: " + response.toString());
            // 获取response的body
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("短信发送响应体: " + responseBody);

            // 这里可以根据响应体判断发送是否成功
            // 例如，检查响应码是否为200，或者响应体中是否包含成功标识
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateSmsUuid() {
        return "sms:code:" + UUID.randomUUID();
    }


    @Value("${sms.aliyun.expireTime}")
    private Integer expireTime;
    //随机生成验证码key

    public void saveSmsCode(String phone, String code, String smsUuid) {

        Map<String, Object> map =  new HashMap<>();

        map.put("phone", phone);
        map.put("code", code);

        redisTemplate.opsForValue().set(smsUuid, map, expireTime, TimeUnit.MINUTES);

    }

}