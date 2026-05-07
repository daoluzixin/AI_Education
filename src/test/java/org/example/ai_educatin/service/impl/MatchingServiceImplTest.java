package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.example.ai_educatin.vo.CandidateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MatchingServiceImpl 对抗测试
 *
 * 覆盖策略（对标 Harness Part3 对抗测试设计）：
 * - 正常路径：标准匹配场景
 * - 边界数据：null/空串/超长字符串/极端评分
 * - 异常路径：非法状态、不存在的需求
 * - 业务规则：评分排序正确性、limit 限制、饱和度惩罚
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceImplTest {

    @Mock
    private DemandService demandService;

    @Mock
    private StudentProfileMapper studentProfileMapper;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Mock
    private SubjectMappingConfig subjectMappingConfig;

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private Demand mockDemand;
    private StudentProfile student985;
    private StudentProfile student211;
    private StudentProfile studentNormal;

    @BeforeEach
    void setUp() {
        mockDemand = new Demand();
        mockDemand.setId(1L);
        mockDemand.setDemandNo("REQ202605070001");
        mockDemand.setCity("北京");
        mockDemand.setDemandType("SUBJECT_TUTOR");
        mockDemand.setStatus(DemandStatus.MATCHING.getCode());
        mockDemand.setBudget("100-150");
        mockDemand.setExpectations("耐心,有经验,数学好");

        student985 = buildStudent(101L, "S001", "张三", "北京大学", "数学,物理,化学", "耐心,认真负责,有经验", "100-150");
        student211 = buildStudent(102L, "S002", "李四", "北京邮电大学", "英语,语文", "活泼,善于沟通", "80-120");
        studentNormal = buildStudent(103L, "S003", "王五", "北京联合大学", "体育,美术", "开朗", "200-300");
    }

    private StudentProfile buildStudent(Long id, String no, String name, String school,
                                         String subjects, String tags, String rate) {
        StudentProfile s = new StudentProfile();
        s.setId(id);
        s.setStudentNo(no);
        s.setRealName(name);
        s.setGender(1);
        s.setSchoolName(school);
        s.setGrade("大三");
        s.setCity("北京");
        s.setSubjects(subjects);
        s.setTags(tags);
        s.setHourlyRate(rate);
        s.setReviewStatus(ReviewStatus.APPROVED.getCode());
        return s;
    }

    // ==================== 正常路径 ====================

    @Nested
    @DisplayName("正常匹配场景")
    class NormalCases {

        @Test
        @DisplayName("985学生科目/标签/学校/预算全匹配，评分应为最高")
        void fullMatch_985Student_highestScore() {
            setupStandardMocks(Arrays.asList(student985, student211, studentNormal));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(3, result.size());
            assertEquals("张三", result.get(0).getRealName());
            assertTrue(result.get(0).getTotalScore() > result.get(1).getTotalScore());
            assertTrue(result.get(1).getTotalScore() > result.get(2).getTotalScore());
        }

        @Test
        @DisplayName("评分明细包含所有五个维度")
        void scoreDetail_containsAllDimensions() {
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            Map<String, Integer> detail = result.get(0).getScoreDetail();
            assertEquals(5, detail.size());
            assertTrue(detail.containsKey("科目匹配"));
            assertTrue(detail.containsKey("标签匹配"));
            assertTrue(detail.containsKey("学校层次"));
            assertTrue(detail.containsKey("预算匹配"));
            assertTrue(detail.containsKey("接单余量"));
        }

        @Test
        @DisplayName("limit 参数正确限制返回数量")
        void limit_restrictsResultCount() {
            setupStandardMocks(Arrays.asList(student985, student211, studentNormal));

            List<CandidateVO> result = matchingService.findCandidates(1L, 2);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("PENDING 状态的需求也允许匹配")
        void pendingDemand_allowsMatching() {
            mockDemand.setStatus(DemandStatus.PENDING.getCode());
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(1, result.size());
        }
    }

    // ==================== 异常路径 ====================

    @Nested
    @DisplayName("异常场景")
    class ExceptionCases {

        @Test
        @DisplayName("需求不存在 → 抛 404 BusinessException")
        void demandNotFound_throws404() {
            when(demandService.getById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> matchingService.findCandidates(999L, 10));
            assertEquals(404, ex.getCode());
        }

        @Test
        @DisplayName("需求状态为 CLOSED → 抛 400 BusinessException")
        void closedDemand_throws400() {
            Demand closed = new Demand();
            closed.setId(2L);
            closed.setStatus(DemandStatus.CLOSED.getCode());
            when(demandService.getById(2L)).thenReturn(closed);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> matchingService.findCandidates(2L, 10));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("需求状态为 RECOMMENDED → 抛 400 BusinessException")
        void recommendedDemand_throws400() {
            Demand recommended = new Demand();
            recommended.setId(3L);
            recommended.setStatus(DemandStatus.RECOMMENDED.getCode());
            when(demandService.getById(3L)).thenReturn(recommended);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> matchingService.findCandidates(3L, 10));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("同城无候选学生 → 返回空列表而非 null")
        void noCandidatesInCity_returnsEmptyList() {
            when(demandService.getById(1L)).thenReturn(mockDemand);
            when(studentProfileMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== 边界数据对抗 ====================

    @Nested
    @DisplayName("边界数据对抗")
    class BoundaryCases {

        @Test
        @DisplayName("学生 subjects 为 null → 科目匹配得 0 分而非 NPE")
        void nullSubjects_zeroScoreNotNPE() {
            student985.setSubjects(null);
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getScoreDetail().get("科目匹配").intValue());
        }

        @Test
        @DisplayName("学生 subjects 为空串 → 科目匹配得 0 分")
        void emptySubjects_zeroScore() {
            student985.setSubjects("   ");
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(0, result.get(0).getScoreDetail().get("科目匹配").intValue());
        }

        @Test
        @DisplayName("学生 tags 为 null → 标签匹配得 0 分而非 NPE")
        void nullTags_zeroScoreNotNPE() {
            student985.setTags(null);
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(0, result.get(0).getScoreDetail().get("标签匹配").intValue());
        }

        @Test
        @DisplayName("需求 expectations 为 null → 标签匹配得 0 分")
        void nullExpectations_zeroTagScore() {
            mockDemand.setExpectations(null);
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(0, result.get(0).getScoreDetail().get("标签匹配").intValue());
        }

        @Test
        @DisplayName("学生 hourlyRate 为 null → 预算匹配得半分而非 NPE")
        void nullHourlyRate_halfScoreNotNPE() {
            student985.setHourlyRate(null);
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(7, result.get(0).getScoreDetail().get("预算匹配").intValue()); // 15/2 = 7
        }

        @Test
        @DisplayName("需求 budget 为非法格式 → 预算匹配得半分而非异常")
        void invalidBudgetFormat_halfScore() {
            mockDemand.setBudget("免费");
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(7, result.get(0).getScoreDetail().get("预算匹配").intValue());
        }

        @Test
        @DisplayName("学生 schoolName 为 null → 学校层次得 0 分而非 NPE")
        void nullSchoolName_zeroSchoolScore() {
            student985.setSchoolName(null);
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(0, result.get(0).getScoreDetail().get("学校层次").intValue());
        }

        @Test
        @DisplayName("预算为 200+ 格式 → 能正确解析")
        void budgetPlusFormat_parsedCorrectly() {
            mockDemand.setBudget("200+");
            student985.setHourlyRate("250-350");
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            // 200+ 解析为 [200, 300]，student [250, 350]，有交集
            assertTrue(result.get(0).getScoreDetail().get("预算匹配") > 0);
        }
    }

    // ==================== 饱和度对抗 ====================

    @Nested
    @DisplayName("饱和度评分对抗")
    class SaturationCases {

        @Test
        @DisplayName("0 个活跃推荐 → 接单余量满分 20")
        void zeroActiveRecs_fullSaturationScore() {
            setupStandardMocks(Arrays.asList(student985));

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(20, result.get(0).getScoreDetail().get("接单余量").intValue());
            assertEquals(0, result.get(0).getActiveRecommendCount());
        }

        @Test
        @DisplayName("5 个以上活跃推荐 → 接单余量得 0 分")
        void overSaturation_zeroScore() {
            when(demandService.getById(1L)).thenReturn(mockDemand);
            when(studentProfileMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(student985));
            when(subjectMappingConfig.getSubjectsForDemandType("SUBJECT_TUTOR"))
                    .thenReturn(new HashSet<>(Arrays.asList("数学", "物理", "化学")));
            when(subjectMappingConfig.isUnrestricted("SUBJECT_TUTOR")).thenReturn(false);

            // 6 条推荐记录
            List<Recommendation> recs = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                Recommendation rec = new Recommendation();
                rec.setStudentId(101L);
                recs.add(rec);
            }
            when(recommendationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(recs);

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(0, result.get(0).getScoreDetail().get("接单余量").intValue());
        }

        @Test
        @DisplayName("饱和度高的学生排名被低饱和度学生超越")
        void highSaturation_rankedLower() {
            when(demandService.getById(1L)).thenReturn(mockDemand);
            // 两个相同实力学生，只有饱和度不同
            StudentProfile freshStudent = buildStudent(104L, "S004", "赵六", "北京大学",
                    "数学,物理,化学", "耐心,认真负责,有经验", "100-150");
            when(studentProfileMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(student985, freshStudent));
            when(subjectMappingConfig.getSubjectsForDemandType("SUBJECT_TUTOR"))
                    .thenReturn(new HashSet<>(Arrays.asList("数学", "物理", "化学")));
            when(subjectMappingConfig.isUnrestricted("SUBJECT_TUTOR")).thenReturn(false);

            // student985 有4条推荐，freshStudent 有0条
            List<Recommendation> recs = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Recommendation rec = new Recommendation();
                rec.setStudentId(101L);
                recs.add(rec);
            }
            when(recommendationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(recs);

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            // freshStudent (id=104) 饱和度为0，应排在前面
            assertEquals(104L, result.get(0).getStudentId().longValue());
        }
    }

    // ==================== OTHER 类型对抗 ====================

    @Nested
    @DisplayName("OTHER 类型需求对抗")
    class OtherTypeCases {

        @Test
        @DisplayName("OTHER 类型 → 科目匹配给基础分 15（满分的一半）")
        void otherType_baseSubjectScore() {
            mockDemand.setDemandType("OTHER");
            when(demandService.getById(1L)).thenReturn(mockDemand);
            when(studentProfileMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(student985));
            when(recommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(subjectMappingConfig.getSubjectsForDemandType("OTHER"))
                    .thenReturn(Collections.emptySet());
            when(subjectMappingConfig.isUnrestricted("OTHER")).thenReturn(true);

            List<CandidateVO> result = matchingService.findCandidates(1L, 10);

            assertEquals(15, result.get(0).getScoreDetail().get("科目匹配").intValue());
        }
    }

    // ==================== 辅助方法 ====================

    private void setupStandardMocks(List<StudentProfile> candidates) {
        when(demandService.getById(1L)).thenReturn(mockDemand);
        when(studentProfileMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(candidates);
        when(recommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(subjectMappingConfig.getSubjectsForDemandType("SUBJECT_TUTOR"))
                .thenReturn(new HashSet<>(Arrays.asList("数学", "语文", "英语", "物理", "化学", "生物", "历史", "地理", "政治")));
        when(subjectMappingConfig.isUnrestricted("SUBJECT_TUTOR")).thenReturn(false);
    }
}
