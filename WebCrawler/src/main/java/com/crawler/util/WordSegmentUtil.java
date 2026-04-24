package com.crawler.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class WordSegmentUtil {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一",
            "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
            "没有", "看", "好", "自己", "这", "他", "她", "它", "们", "那", "些",
            "什么", "怎么", "可以", "因为", "所以", "如果", "虽然", "但是", "而且",
            "或者", "还是", "只是", "但", "不是", "这个", "那个", "哪里", "那里",
            "这里", "我们", "他们", "她们", "它们", "如何", "哪个", "哪些",
            "为什么", "怎样", "谁", "几", "多少", "非常", "十分", "比较",
            "最", "更", "越来越", "特别", "为了", "关于", "对于", "通过", "按照",
            "根据", "从", "被", "把", "对", "让", "向", "往", "与", "以", "及",
            "等", "之", "于", "为", "所", "其", "各", "每", "某", "该", "本",
            "将", "已", "已经", "还", "又", "再", "才", "刚", "刚刚", "正在",
            "马上", "立刻", "顿时", "逐渐", "渐渐", "终于", "曾经",
            "曾", "正", "将", "要", "能", "能够", "应该", "应当", "必须",
            "需要", "得", "可能", "也许", "大概", "一定", "肯定", "当然",
            "确实", "的确", "其实", "实际上", "自然", "显然", "明显",
            "原来", "本来", "原本", "根本", "基本", "主要", "重要", "完全",
            "全部", "所有", "一切", "任何", "每个", "各个", "许多", "很多",
            "大量", "一些", "少数", "部分", "不少", "其中", "之间", "之后",
            "之前", "以上", "以下", "以内", "以外", "当中", "中间", "同时",
            "此外", "另外", "还有", "以及", "等等", "包括", "涉及", "提到",
            "指出", "表示", "认为", "说明", "表明", "反映", "显示", "体现",
            "证明", "意味着", "称", "强调", "报道", "进行", "开始", "结束",
            "完成", "实现", "达到", "保持", "继续", "存在", "出现", "发生",
            "引起", "造成", "导致", "使得", "成为", "作为", "得到", "取得",
            "获得", "给予", "赋予", "提供", "带来", "产生", "形成", "建立",
            "建设", "发展", "开展", "展开", "举行", "召开", "举办", "组织",
            "参加", "参与", "加入", "进入", "通过", "经过", "受到", "遭到",
            "遇到", "面临", "面对", "针对", "应对", "处理", "解决", "负责",
            "承担", "接受", "管理", "监督", "检查", "调查", "研究", "分析",
            "评估", "评价", "总结", "制定", "编写", "设置", "规定", "确定",
            "决定", "贯彻", "执行", "落实", "实施", "推行", "推广", "实行",
            "采取", "采用", "选用", "使用", "利用", "运用", "应用", "发挥",
            "进行", "关于", "其中", "作", "做", "对", "被", "从", "第",
            "其", "而", "所", "当", "中", "或", "与", "个", "年", "月",
            "日", "时", "分", "秒", "元", "万", "亿"
    ));

    private static final int MIN_WORD_LENGTH = 2;
    private static final int TOP_N = 100;

    private static final Set<String> KEYWORD_NATURES = new HashSet<>(Arrays.asList(
            "n", "v", "a", "vn", "an", "vd", "ad", "nz", "j", "l", "i"
    ));

    private static final Set<String> ENTITY_NATURES = new HashSet<>(Arrays.asList(
            "nr", "nr1", "nr2", "nrf", "ns", "nsf", "nt", "nz"
    ));

    public static Map<String, Integer> segmentKeywords(String text) {
        if (text == null || text.isBlank()) return Collections.emptyMap();
        List<Term> terms = HanLP.segment(text);
        Map<String, Integer> freq = new HashMap<>();
        for (Term term : terms) {
            String word = term.word.trim();
            String nature = term.nature.toString();
            if (word.length() < MIN_WORD_LENGTH) continue;
            if (STOP_WORDS.contains(word)) continue;
            if (!isChineseOrAlpha(word)) continue;
            if (!KEYWORD_NATURES.contains(nature)) continue;
            freq.merge(word, 1, Integer::sum);
        }
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(TOP_N)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    public static Map<String, Integer> segmentEntities(String text) {
        if (text == null || text.isBlank()) return Collections.emptyMap();
        List<Term> terms = HanLP.segment(text);
        Map<String, Integer> freq = new HashMap<>();
        for (Term term : terms) {
            String word = term.word.trim();
            String nature = term.nature.toString();
            if (word.length() < MIN_WORD_LENGTH) continue;
            if (STOP_WORDS.contains(word)) continue;
            if (!ENTITY_NATURES.contains(nature)) continue;
            freq.merge(word, 1, Integer::sum);
        }
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(TOP_N)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    public static Map<String, Integer> mergeWeighted(Map<String, Integer> titleWords,
                                                       Map<String, Integer> contentWords,
                                                       int titleWeight) {
        Map<String, Integer> merged = new HashMap<>(contentWords);
        for (Map.Entry<String, Integer> entry : titleWords.entrySet()) {
            merged.merge(entry.getKey(), entry.getValue() * titleWeight, Integer::sum);
        }
        return merged.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(TOP_N)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    private static boolean isChineseOrAlpha(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isLetter(c)) return true;
        }
        return false;
    }
}
