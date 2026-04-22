package com.crawler.entity.dto;

import cn.hutool.json.JSONUtil;
import com.crawler.entity.SpecialAlertSetting;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * JSON字段（keyWord / params / timeRange）在构造时自动反序列化为结构化对象，
 * Jackson 序列化后前端直接收到嵌套对象，无需额外处理。
 */
@Data
public class SpecialAlertDto {

    private Integer crawlerId;      // 专题ID
    private String crawlerName;     // 专题名称
    private Integer triggerState;   // 触发状态，0：实时、1：定时、2：定量
    private String targetSource;     // 数据源
    private Object keyWord;         // {"keywordGroups":[["词A","词B"],["词C"]]}
    private Object params;          // {"searchFields":1,"sortField":0}，可为 null
    private Integer frequency;      // 预警频率，0：实时，1：定时，2：定量
    private Integer alertTrigger;   // 定量触发阈值（frequency=2时必填）
    private Object timeRange;       // {"weekdays":[...],"time":{"start":"...","end":"..."}}，实时模式为 null
    private Integer alertMethod;    // 预警方式，0：全部、1：站内信、2：邮箱
    private Integer state;          // 状态：0：停用 1：启用

    /**
     * 从实体构建DTO。
     * 三个 JSON 字符串字段通过 {@link #parseJson(String)} 转为结构化对象，
     * 若原始值为 null 或空字符串则保持 null，不抛出异常。
     */
    public SpecialAlertDto(SpecialAlertSetting c) {
        this.crawlerId    = c.getAlertId();
        this.crawlerName  = c.getAlertName();
        this.triggerState = c.getTriggerState();
        this.targetSource = c.getTargetSource();
        this.keyWord      = parseJson(c.getKeyWord());
        this.params       = parseJson(c.getParams());
        this.frequency    = c.getFrequency();
        this.alertTrigger = c.getAlertTrigger();
        this.timeRange    = parseJson(c.getTimeRange());
        this.alertMethod  = c.getAlertMethod();
        this.state        = c.getState();
    }

    /**
     * 将 JSON 字符串安全地解析为结构化对象（Map 或 List）。
     * - 以 '{' 开头 → 解析为 JSONObject（序列化后是嵌套对象）
     * - 以 '[' 开头 → 解析为 JSONArray（序列化后是数组）
     * - null / 空字符串 → 返回 null
     *
     * @param json 数据库中存储的原始 JSON 字符串
     * @return 结构化对象，或 null
     */
    private static Object parseJson(String json) {
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