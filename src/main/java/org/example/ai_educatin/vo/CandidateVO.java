package org.example.ai_educatin.vo;

import lombok.Data;

import java.util.Map;

/**
 * 候选学生 VO - 包含匹配评分明细，供运营参考
 */
@Data
public class CandidateVO {

    /** 学生档案ID */
    private Long studentId;

    /** 学生编号 */
    private String studentNo;

    /** 真实姓名 */
    private String realName;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 学校名称 */
    private String schoolName;

    /** 年级 */
    private String grade;

    /** 城市 */
    private String city;

    /** 擅长方向(逗号分隔) */
    private String subjects;

    /** 个人标签 */
    private String tags;

    /** 个人照片 */
    private String avatar;

    /** 自我介绍 */
    private String introduction;

    /** 期望时薪 */
    private String hourlyRate;

    /** 综合匹配得分(0~100) */
    private int totalScore;

    /** 各维度得分明细 */
    private Map<String, Integer> scoreDetail;

    /** 当前被推荐中的活跃需求数 */
    private int activeRecommendCount;
}
