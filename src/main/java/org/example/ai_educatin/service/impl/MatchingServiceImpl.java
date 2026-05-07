package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_educatin.common.config.SubjectMappingConfig;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.mapper.StudentProfileMapper;
import org.example.ai_educatin.service.DemandService;
import org.example.ai_educatin.service.MatchingService;
import org.example.ai_educatin.vo.CandidateVO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则匹配引擎实现
 *
 * 评分维度与权重（满分100）：
 * - 科目匹配: 30分（demandType对应的学科集合 vs student.subjects 交集比例）
 * - 标签匹配: 20分（demand.expectations关键词 vs student.tags 命中数）
 * - 学校层次: 15分（985=15, 211=10, 普通=5）
 * - 预算匹配: 15分（demand.budget vs student.hourlyRate 是否重叠）
 * - 接单饱和度: 20分（推荐活跃数越少分越高，0个=20分，递减）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final DemandService demandService;
    private final StudentProfileMapper studentProfileMapper;
    private final RecommendationMapper recommendationMapper;
    private final SubjectMappingConfig subjectMappingConfig;

    /** 各维度满分 */
    private static final int SCORE_SUBJECT = 30;
    private static final int SCORE_TAG = 20;
    private static final int SCORE_SCHOOL = 15;
    private static final int SCORE_BUDGET = 15;
    private static final int SCORE_SATURATION = 20;

    /** 饱和度阈值: 超过此数量的活跃推荐，得分为0 */
    private static final int SATURATION_THRESHOLD = 5;

    /** 985高校关键词集合（部分示例，生产环境应查字典表） */
    private static final Set<String> SCHOOL_985 = new HashSet<>(Arrays.asList(
            "北京大学", "清华大学", "复旦大学", "上海交通大学", "浙江大学",
            "中国科学技术大学", "南京大学", "武汉大学", "华中科技大学", "中山大学",
            "西安交通大学", "哈尔滨工业大学", "同济大学", "北京航空航天大学", "北京理工大学",
            "天津大学", "南开大学", "山东大学", "中南大学", "东南大学",
            "四川大学", "厦门大学", "吉林大学", "大连理工大学", "电子科技大学",
            "重庆大学", "湖南大学", "中国人民大学", "兰州大学", "西北工业大学",
            "华南理工大学", "中国农业大学", "国防科技大学", "中央民族大学",
            "华东师范大学", "北京师范大学", "中国海洋大学", "东北大学", "西北农林科技大学"
    ));

    /** 211高校补充关键词（非985的211，部分示例） */
    private static final Set<String> SCHOOL_211_EXTRA = new HashSet<>(Arrays.asList(
            "上海财经大学", "中央财经大学", "对外经济贸易大学", "北京邮电大学",
            "华北电力大学", "中国政法大学", "北京外国语大学", "上海外国语大学",
            "南京航空航天大学", "南京理工大学", "河海大学", "苏州大学",
            "西南财经大学", "西南交通大学", "暨南大学", "华南师范大学",
            "陕西师范大学", "武汉理工大学", "华中师范大学", "郑州大学",
            "南昌大学", "云南大学", "广西大学", "贵州大学"
    ));

    @Override
    public List<CandidateVO> findCandidates(Long demandId, int limit) {
        // 1. 获取需求详情
        Demand demand = demandService.getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        DemandStatus status = DemandStatus.fromCode(demand.getStatus());
        if (status != DemandStatus.MATCHING && status != DemandStatus.PENDING) {
            throw new BusinessException(400, "需求当前状态不适合匹配，当前: " + status.getDesc());
        }

        // 2. 硬过滤: 同城市 + 审核通过
        List<StudentProfile> candidates = studentProfileMapper.selectList(
                new LambdaQueryWrapper<StudentProfile>()
                        .eq(StudentProfile::getCity, demand.getCity())
                        .eq(StudentProfile::getReviewStatus, ReviewStatus.APPROVED.getCode())
        );

        if (candidates.isEmpty()) {
            log.info("需求[{}]在城市[{}]无可匹配的已审核学生", demand.getDemandNo(), demand.getCity());
            return Collections.emptyList();
        }

        // 3. 查询每位候选学生的活跃推荐数
        Map<Long, Integer> activeCountMap = getActiveRecommendCounts(candidates);

        // 4. 对每位候选学生计算多维度评分
        Set<String> demandSubjects = subjectMappingConfig.getSubjectsForDemandType(demand.getDemandType());
        boolean unrestricted = subjectMappingConfig.isUnrestricted(demand.getDemandType());

        List<CandidateVO> scoredList = candidates.stream()
                .map(student -> scoreCandidate(student, demand, demandSubjects, unrestricted, activeCountMap))
                .sorted(Comparator.comparingInt(CandidateVO::getTotalScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return scoredList;
    }

    /**
     * 对单个学生计算匹配评分
     */
    private CandidateVO scoreCandidate(StudentProfile student, Demand demand,
                                        Set<String> demandSubjects, boolean unrestricted,
                                        Map<Long, Integer> activeCountMap) {
        Map<String, Integer> detail = new LinkedHashMap<>();

        // === 维度1: 科目匹配 (30分) ===
        int subjectScore = calcSubjectScore(student.getSubjects(), demandSubjects, unrestricted);
        detail.put("科目匹配", subjectScore);

        // === 维度2: 标签匹配 (20分) ===
        int tagScore = calcTagScore(student.getTags(), demand.getExpectations());
        detail.put("标签匹配", tagScore);

        // === 维度3: 学校层次 (15分) ===
        int schoolScore = calcSchoolScore(student.getSchoolName());
        detail.put("学校层次", schoolScore);

        // === 维度4: 预算匹配 (15分) ===
        int budgetScore = calcBudgetScore(student.getHourlyRate(), demand.getBudget());
        detail.put("预算匹配", budgetScore);

        // === 维度5: 接单饱和度 (20分) ===
        int activeCount = activeCountMap.getOrDefault(student.getId(), 0);
        int saturationScore = calcSaturationScore(activeCount);
        detail.put("接单余量", saturationScore);

        int total = subjectScore + tagScore + schoolScore + budgetScore + saturationScore;

        // 构建 VO
        CandidateVO vo = new CandidateVO();
        vo.setStudentId(student.getId());
        vo.setStudentNo(student.getStudentNo());
        vo.setRealName(student.getRealName());
        vo.setGender(student.getGender());
        vo.setSchoolName(student.getSchoolName());
        vo.setGrade(student.getGrade());
        vo.setCity(student.getCity());
        vo.setSubjects(student.getSubjects());
        vo.setTags(student.getTags());
        vo.setAvatar(student.getAvatar());
        vo.setIntroduction(student.getIntroduction());
        vo.setHourlyRate(student.getHourlyRate());
        vo.setTotalScore(total);
        vo.setScoreDetail(detail);
        vo.setActiveRecommendCount(activeCount);

        return vo;
    }

    /**
     * 科目匹配评分: student.subjects 与 demandType 对应学科的交集比例
     * 满分30分
     */
    private int calcSubjectScore(String studentSubjects, Set<String> demandSubjects, boolean unrestricted) {
        if (unrestricted) {
            // OTHER 类型不做科目限制，给基础分
            return SCORE_SUBJECT / 2;
        }
        if (studentSubjects == null || studentSubjects.isBlank() || demandSubjects.isEmpty()) {
            return 0;
        }

        Set<String> studentSet = Arrays.stream(studentSubjects.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        long matchCount = studentSet.stream()
                .filter(demandSubjects::contains)
                .count();

        if (matchCount == 0) {
            return 0;
        }

        // 交集比例 = 匹配数 / 需求学科总数，按比例给分
        double ratio = (double) matchCount / demandSubjects.size();
        return (int) Math.round(ratio * SCORE_SUBJECT);
    }

    /**
     * 标签匹配评分: student.tags 中命中 demand.expectations 关键词的数量
     * 满分20分
     */
    private int calcTagScore(String studentTags, String expectations) {
        if (studentTags == null || studentTags.isBlank() || expectations == null || expectations.isBlank()) {
            return 0;
        }

        String[] tags = studentTags.split(",");
        int hitCount = 0;
        String lowerExpect = expectations.toLowerCase();

        for (String tag : tags) {
            String trimmedTag = tag.trim().toLowerCase();
            if (!trimmedTag.isEmpty() && lowerExpect.contains(trimmedTag)) {
                hitCount++;
            }
        }

        if (hitCount == 0) {
            return 0;
        }

        // 最多5个标签全部命中得满分
        return Math.min(hitCount * (SCORE_TAG / 5 + 1), SCORE_TAG);
    }

    /**
     * 学校层次评分: 985=15分, 211=10分, 普通=5分
     * 满分15分
     */
    private int calcSchoolScore(String schoolName) {
        if (schoolName == null || schoolName.isBlank()) {
            return 0;
        }
        if (SCHOOL_985.contains(schoolName)) {
            return SCORE_SCHOOL;
        }
        if (SCHOOL_211_EXTRA.contains(schoolName)) {
            return (int) (SCORE_SCHOOL * 0.67);
        }
        // 普通高校基础分
        return (int) (SCORE_SCHOOL * 0.33);
    }

    /**
     * 预算匹配评分: demand.budget 与 student.hourlyRate 的重叠度
     * 满分15分
     */
    private int calcBudgetScore(String studentRate, String demandBudget) {
        if (studentRate == null || studentRate.isBlank() || demandBudget == null || demandBudget.isBlank()) {
            // 任一方未填写，给半分（不做惩罚也不加分）
            return SCORE_BUDGET / 2;
        }

        // 解析预算范围，格式: "50-100" / "100-150" / "150-200" / "200+"
        int[] demandRange = parseRange(demandBudget);
        int[] studentRange = parseRange(studentRate);

        if (demandRange == null || studentRange == null) {
            return SCORE_BUDGET / 2;
        }

        // 判断区间是否有交集
        if (studentRange[0] <= demandRange[1] && studentRange[1] >= demandRange[0]) {
            // 完全包含或大幅重叠
            int overlap = Math.min(studentRange[1], demandRange[1]) - Math.max(studentRange[0], demandRange[0]);
            int demandWidth = demandRange[1] - demandRange[0];
            if (demandWidth <= 0) {
                return SCORE_BUDGET;
            }
            double ratio = (double) overlap / demandWidth;
            return (int) Math.round(ratio * SCORE_BUDGET);
        }

        // 无交集
        return 0;
    }

    /**
     * 接单饱和度评分: 活跃推荐越少得分越高
     * 满分20分，0个活跃=20分，每增加1个减4分，超过5个=0分
     */
    private int calcSaturationScore(int activeCount) {
        if (activeCount >= SATURATION_THRESHOLD) {
            return 0;
        }
        return SCORE_SATURATION - (activeCount * (SCORE_SATURATION / SATURATION_THRESHOLD));
    }

    /**
     * 查询每位候选学生的活跃推荐数（关联的需求状态为 MATCHING 或 RECOMMENDED）
     */
    private Map<Long, Integer> getActiveRecommendCounts(List<StudentProfile> candidates) {
        List<Long> studentIds = candidates.stream()
                .map(StudentProfile::getId)
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 查询这些学生的所有推荐记录
        List<Recommendation> allRecs = recommendationMapper.selectList(
                new LambdaQueryWrapper<Recommendation>()
                        .in(Recommendation::getStudentId, studentIds)
        );

        // 统计每位学生的推荐数（简化版：统计推荐记录总数作为活跃度参考）
        Map<Long, Integer> countMap = new HashMap<>();
        for (Recommendation rec : allRecs) {
            countMap.merge(rec.getStudentId(), 1, Integer::sum);
        }

        return countMap;
    }

    /**
     * 解析预算/时薪范围字符串为 [min, max] 数组
     * 支持格式: "50-100", "200+"
     */
    private int[] parseRange(String rangeStr) {
        if (rangeStr == null || rangeStr.isBlank()) {
            return null;
        }

        rangeStr = rangeStr.trim();

        if (rangeStr.endsWith("+")) {
            // "200+" 格式
            try {
                int min = Integer.parseInt(rangeStr.substring(0, rangeStr.length() - 1));
                return new int[]{min, min + 100}; // 给一个合理上限
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String[] parts = rangeStr.split("-");
        if (parts.length == 2) {
            try {
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return new int[]{min, max};
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
