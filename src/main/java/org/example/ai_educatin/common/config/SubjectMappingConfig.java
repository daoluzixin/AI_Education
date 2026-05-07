package org.example.ai_educatin.common.config;

import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * 需求类型 → 学科映射配置
 * 将 demand.demand_type 映射为对应的学科集合，用于与 student_profile.subjects 做交集匹配
 */
@Configuration
public class SubjectMappingConfig {

    private static final Map<String, Set<String>> DEMAND_TYPE_SUBJECTS = new HashMap<>();

    static {
        // 学科辅导: 覆盖主要学科
        DEMAND_TYPE_SUBJECTS.put("SUBJECT_TUTOR", new HashSet<>(Arrays.asList(
                "MATH", "CHINESE", "ENGLISH", "PHYSICS", "CHEMISTRY",
                "BIOLOGY", "HISTORY", "GEOGRAPHY", "POLITICS"
        )));

        // 兴趣培养: 艺术/体育/编程/写作
        DEMAND_TYPE_SUBJECTS.put("INTEREST", new HashSet<>(Arrays.asList(
                "MUSIC", "ART", "SPORTS", "PROGRAMMING", "WRITING",
                "DANCE", "CALLIGRAPHY"
        )));

        // 竞赛辅导: 五大学科竞赛 + 信息学
        DEMAND_TYPE_SUBJECTS.put("COMPETITION", new HashSet<>(Arrays.asList(
                "MATH", "PHYSICS", "CHEMISTRY", "BIOLOGY", "PROGRAMMING"
        )));

        // 升学指导: 语数英通识 + 面试
        DEMAND_TYPE_SUBJECTS.put("ADMISSION", new HashSet<>(Arrays.asList(
                "MATH", "CHINESE", "ENGLISH", "INTERVIEW"
        )));

        // 其他: 空集合，不做科目限制
        DEMAND_TYPE_SUBJECTS.put("OTHER", Collections.emptySet());
    }

    /**
     * 获取某需求类型对应的学科集合
     *
     * @param demandType 需求类型枚举值
     * @return 对应学科集合，未知类型返回空集合
     */
    public Set<String> getSubjectsForDemandType(String demandType) {
        return DEMAND_TYPE_SUBJECTS.getOrDefault(demandType, Collections.emptySet());
    }

    /**
     * 判断某需求类型是否不做科目限制（如 OTHER）
     */
    public boolean isUnrestricted(String demandType) {
        Set<String> subjects = DEMAND_TYPE_SUBJECTS.get(demandType);
        return subjects == null || subjects.isEmpty();
    }
}
