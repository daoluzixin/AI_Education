package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.entity.dto.admin.RecommendDTO;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.service.DemandService;
import org.example.ai_educatin.service.StudentProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RecommendationServiceImpl 对抗测试
 *
 * 核心验证：推荐上限 5 人业务规则（对标 03-constraints.md 第5节）
 * 覆盖策略：
 * - 正常路径：1~5 人推荐
 * - 边界：恰好 5 人
 * - 溢出：6 人、100 人
 * - 异常：需求不存在、状态不合法、学生未审核
 *
 * 注意：RecommendationServiceImpl 继承 ServiceImpl，使用 @Spy 绕过 BaseMapper 调用。
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private DemandService demandService;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Spy
    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Demand matchingDemand;

    @BeforeEach
    void setUp() {
        matchingDemand = new Demand();
        matchingDemand.setId(1L);
        matchingDemand.setStatus(DemandStatus.MATCHING.getCode());
    }

    private StudentProfile approvedStudent(Long id) {
        StudentProfile s = new StudentProfile();
        s.setId(id);
        s.setRealName("学生" + id);
        s.setReviewStatus(ReviewStatus.APPROVED.getCode());
        return s;
    }

    private StudentProfile unapprovedStudent(Long id) {
        StudentProfile s = new StudentProfile();
        s.setId(id);
        s.setRealName("未审核学生" + id);
        s.setReviewStatus(ReviewStatus.PENDING_REVIEW.getCode());
        return s;
    }

    // ==================== 推荐上限规则 ====================

    @Nested
    @DisplayName("推荐数量上限校验（核心业务规则：最多5人）")
    class RecommendLimitCases {

        @Test
        @DisplayName("推荐 6 人 → 抛 400 BusinessException")
        void sixStudents_throwsException() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("5"));
        }

        @Test
        @DisplayName("推荐 100 人 → 抛 400 BusinessException（极端溢出）")
        void hundredStudents_throwsException() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            List<Long> ids = LongStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());
            dto.setStudentIds(ids);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("推荐恰好 5 人（边界值） → 通过校验，正常执行")
        void exactlyFiveStudents_passes() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);
            for (long i = 1; i <= 5; i++) {
                when(studentProfileService.getById(i)).thenReturn(approvedStudent(i));
            }

            // Mock ServiceImpl 继承的 remove 和 save 方法
            doReturn(true).when(recommendationService).remove(any(LambdaQueryWrapper.class));
            doReturn(true).when(recommendationService).save(any(Recommendation.class));
            when(demandService.updateById(any(Demand.class))).thenReturn(true);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));

            assertDoesNotThrow(() -> recommendationService.configureRecommendation(dto, 100L));
        }

        @Test
        @DisplayName("推荐 1 人 → 正常通过")
        void oneStudent_passes() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);
            when(studentProfileService.getById(1L)).thenReturn(approvedStudent(1L));

            doReturn(true).when(recommendationService).remove(any(LambdaQueryWrapper.class));
            doReturn(true).when(recommendationService).save(any(Recommendation.class));
            when(demandService.updateById(any(Demand.class))).thenReturn(true);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(1L));

            assertDoesNotThrow(() -> recommendationService.configureRecommendation(dto, 100L));
        }
    }

    // ==================== 需求状态校验 ====================

    @Nested
    @DisplayName("需求状态校验")
    class DemandStatusCases {

        @Test
        @DisplayName("需求不存在 → 抛 404")
        void demandNotFound_throws404() {
            when(demandService.getById(999L)).thenReturn(null);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(999L);
            dto.setStudentIds(Arrays.asList(1L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(404, ex.getCode());
        }

        @Test
        @DisplayName("需求状态为 PENDING → 抛 400（只有 MATCHING/RECOMMENDED 可配置推荐）")
        void pendingDemand_throws400() {
            Demand pending = new Demand();
            pending.setId(2L);
            pending.setStatus(DemandStatus.PENDING.getCode());
            when(demandService.getById(2L)).thenReturn(pending);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(2L);
            dto.setStudentIds(Arrays.asList(1L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("需求状态为 CLOSED → 抛 400")
        void closedDemand_throws400() {
            Demand closed = new Demand();
            closed.setId(3L);
            closed.setStatus(DemandStatus.CLOSED.getCode());
            when(demandService.getById(3L)).thenReturn(closed);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(3L);
            dto.setStudentIds(Arrays.asList(1L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("需求状态为 RECOMMENDED → 允许重新配置推荐")
        void recommendedDemand_allowsReconfigure() {
            Demand recommended = new Demand();
            recommended.setId(4L);
            recommended.setStatus(DemandStatus.RECOMMENDED.getCode());
            when(demandService.getById(4L)).thenReturn(recommended);
            when(studentProfileService.getById(1L)).thenReturn(approvedStudent(1L));

            doReturn(true).when(recommendationService).remove(any(LambdaQueryWrapper.class));
            doReturn(true).when(recommendationService).save(any(Recommendation.class));
            when(demandService.updateById(any(Demand.class))).thenReturn(true);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(4L);
            dto.setStudentIds(Arrays.asList(1L));

            assertDoesNotThrow(() -> recommendationService.configureRecommendation(dto, 100L));
        }
    }

    // ==================== 学生审核状态校验 ====================

    @Nested
    @DisplayName("学生审核状态校验")
    class StudentStatusCases {

        @Test
        @DisplayName("学生未审核通过 → 抛 400")
        void unapprovedStudent_throws400() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);
            when(studentProfileService.getById(1L)).thenReturn(unapprovedStudent(1L));

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(1L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("未审核通过"));
        }

        @Test
        @DisplayName("学生档案不存在 → 抛 404")
        void studentNotFound_throws404() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);
            when(studentProfileService.getById(999L)).thenReturn(null);

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(999L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(404, ex.getCode());
        }

        @Test
        @DisplayName("5人中第3人未审核 → 在第3人处抛异常（顺序检查）")
        void thirdStudentUnapproved_throwsAtThird() {
            when(demandService.getById(1L)).thenReturn(matchingDemand);
            when(studentProfileService.getById(1L)).thenReturn(approvedStudent(1L));
            when(studentProfileService.getById(2L)).thenReturn(approvedStudent(2L));
            when(studentProfileService.getById(3L)).thenReturn(unapprovedStudent(3L));

            RecommendDTO dto = new RecommendDTO();
            dto.setDemandId(1L);
            dto.setStudentIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> recommendationService.configureRecommendation(dto, 100L));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("未审核通过"));
        }
    }
}
