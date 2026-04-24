package com.crawler.util;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommonUtil {
    /**
     * 将 JSON 字符串安全地解析为结构化对象（Map 或 List）。
     * - 以 '{' 开头 → 解析为 JSONObject（序列化后是嵌套对象）
     * - 以 '[' 开头 → 解析为 JSONArray（序列化后是数组）
     * - null / 空字符串 → 返回 null
     *
     * @param json 数据库中存储的原始 JSON 字符串
     * @return 结构化对象，或 null
     */
    public static Object parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) {
            return JSONUtil.parseObj(trimmed);
        }
        if (trimmed.startsWith("[")) {
            return JSONUtil.parseArray(trimmed);
        }
        // 既不是对象也不是数组，原样返回字符串，避免数据丢失
        return json;
    }
}
