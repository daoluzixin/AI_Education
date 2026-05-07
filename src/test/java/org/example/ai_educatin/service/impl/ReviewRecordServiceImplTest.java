package org.example.ai_educatin.service.impl;

import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.ReviewRecord;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.entity.dto.admin.ReviewDTO;
import org.example.ai_educatin.mapper.ReviewRecordMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReviewRecordServiceImpl 单元测试
 *
 * 核心验证：审核操作合法性校验 + 审核记录只增不删不改（INSERT-only）
 * 状态机规则：只有 PENDING_REVIEW(1) 状态才允许审核操作
 * 审核结果：APPROVED(2) / REJECTED(3) / NEED_SUPPLEMENT(4)
 */
@ExtendWith(MockitoExtension.class)
class ReviewRecordServiceImplTest {

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Spy
    @InjectMocks
    private ReviewRecordServiceImpl reviewRecordService;

    private static final Long PROFILE_ID = 100L;
    private static final Long REVIEWER_ID = 1L;
    private static final String REVIEWER_NAME = "admin";

    private StudentProfile pendingReviewProfile;

    @BeforeEach
    void setUp() {
        pendingReviewProfile = buildProfile(PROFILE_ID, ReviewStatus.PENDING_REVIEW);
    }

    private StudentProfile buildProfile(Long id, ReviewStatus status) {
        StudentProfile profile = new StudentProfile();
        profile.setId(id);
        profile.setReviewStatus(status.getCode());
        return profile;
    }

    private ReviewDTO buildDTO(Integer reviewResult, String reviewNote) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewResult(reviewResult);
        dto.setReviewNote(reviewNote);
        return dto;
    }

    // ==================== 审核通过场景 ====================

    @Nested
    @DisplayName("审核通过 (APPROVED)")
    class ApprovedCases {

        @Test
        @DisplayName("RR-001: reviewResult=2, 档案 PENDING_REVIEW -> 审核通过，reviewStatus 变为 2")
        void approved_pendingReview_success() {
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(pendingReviewProfile);
            when(studentProfileService.updateById(any(StudentProfile.class))).thenReturn(true);
            doReturn(true).when(reviewRecordService).save(any(ReviewRecord.class));

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), null);

            assertDoesNotThrow(() ->
                    reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(ReviewStatus.APPROVED.getCode(), pendingReviewProfile.getReviewStatus());
        }

        @Test
        @DisplayName("RR-011: reviewResult=2, reviewNote='可选备注' -> 通过不报错")
        void approved_withOptionalNote_success() {
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(pendingReviewProfile);
            when(studentProfileService.updateById(any(StudentProfile.class))).thenReturn(true);
            doReturn(true).when(reviewRecordService).save(any(ReviewRecord.class));

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), "可选备注");

            assertDoesNotThrow(() ->
                    reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(ReviewStatus.APPROVED.getCode(), pendingReviewProfile.getReviewStatus());
        }
    }

    // ==================== 审核驳回场景 ====================

    @Nested
    @DisplayName("审核驳回 (REJECTED)")
    class RejectedCases {

        @Test
        @DisplayName("RR-002: reviewResult=3, reviewNote='资料不完整' -> 成功，rejectReason 被设置")
        void rejected_withNote_success() {
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(pendingReviewProfile);
            when(studentProfileService.updateById(any(StudentProfile.class))).thenReturn(true);
            doReturn(true).when(reviewRecordService).save(any(ReviewRecord.class));

            ReviewDTO dto = buildDTO(ReviewStatus.REJECTED.getCode(), "资料不完整");

            assertDoesNotThrow(() ->
                    reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(ReviewStatus.REJECTED.getCode(), pendingReviewProfile.getReviewStatus());
            assertEquals("资料不完整", pendingReviewProfile.getRejectReason());
        }

        @Test
        @DisplayName("RR-006: reviewResult=3, reviewNote='' -> 400 驳回时必须填写备注")
        void rejected_emptyNote_throws400() {
            ReviewDTO dto = buildDTO(ReviewStatus.REJECTED.getCode(), "");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("驳回或要求补充时必须填写审核备注"));
        }
    }

    // ==================== 要求补充材料场景 ====================

    @Nested
    @DisplayName("要求补充材料 (NEED_SUPPLEMENT)")
    class NeedSupplementCases {

        @Test
        @DisplayName("RR-003: reviewResult=4, reviewNote='请补充学生证' -> 成功，supplementNote 被设置")
        void needSupplement_withNote_success() {
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(pendingReviewProfile);
            when(studentProfileService.updateById(any(StudentProfile.class))).thenReturn(true);
            doReturn(true).when(reviewRecordService).save(any(ReviewRecord.class));

            ReviewDTO dto = buildDTO(ReviewStatus.NEED_SUPPLEMENT.getCode(), "请补充学生证");

            assertDoesNotThrow(() ->
                    reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(ReviewStatus.NEED_SUPPLEMENT.getCode(), pendingReviewProfile.getReviewStatus());
            assertEquals("请补充学生证", pendingReviewProfile.getSupplementNote());
        }

        @Test
        @DisplayName("RR-007: reviewResult=4, reviewNote=null -> 400 要求补充时必须填写备注")
        void needSupplement_nullNote_throws400() {
            ReviewDTO dto = buildDTO(ReviewStatus.NEED_SUPPLEMENT.getCode(), null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("驳回或要求补充时必须填写审核备注"));
        }
    }

    // ==================== 无效审核结果 ====================

    @Nested
    @DisplayName("无效审核结果校验")
    class InvalidReviewResultCases {

        @Test
        @DisplayName("RR-004: reviewResult=0 -> 400 无效的审核结果")
        void reviewResult_zero_throws400() {
            ReviewDTO dto = buildDTO(0, null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("无效的审核结果"));
        }

        @Test
        @DisplayName("RR-005: reviewResult=99 -> 400 无效的审核结果")
        void reviewResult_99_throws400() {
            ReviewDTO dto = buildDTO(99, null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("无效的审核结果"));
        }
    }

    // ==================== 学生档案不存在 ====================

    @Nested
    @DisplayName("学生档案不存在")
    class ProfileNotFoundCases {

        @Test
        @DisplayName("RR-008: studentProfileId 不存在 -> 404 学生档案不存在")
        void profileNotFound_throws404() {
            when(studentProfileService.getById(999L)).thenReturn(null);

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(999L, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(404, ex.getCode());
            assertTrue(ex.getMessage().contains("学生档案不存在"));
        }
    }

    // ==================== 非法状态下审核 ====================

    @Nested
    @DisplayName("当前状态不允许审核")
    class IllegalStatusCases {

        @Test
        @DisplayName("RR-009: 档案 reviewStatus=DRAFT(0) -> 400 当前状态不允许审核操作")
        void draftStatus_throws400() {
            StudentProfile draftProfile = buildProfile(PROFILE_ID, ReviewStatus.DRAFT);
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(draftProfile);

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("当前状态不允许审核操作"));
        }

        @Test
        @DisplayName("RR-010: 档案 reviewStatus=APPROVED(2) -> 400 当前状态不允许审核操作")
        void approvedStatus_throws400() {
            StudentProfile approvedProfile = buildProfile(PROFILE_ID, ReviewStatus.APPROVED);
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(approvedProfile);

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("当前状态不允许审核操作"));
        }
    }

    // ==================== 审核记录 INSERT-only 验证 ====================

    @Nested
    @DisplayName("审核记录只增验证")
    class InsertOnlyCases {

        @Test
        @DisplayName("RR-012: 审核成功后，save() 被精确调用一次（确保 INSERT-only）")
        void save_calledExactlyOnce() {
            when(studentProfileService.getById(PROFILE_ID)).thenReturn(pendingReviewProfile);
            when(studentProfileService.updateById(any(StudentProfile.class))).thenReturn(true);
            doReturn(true).when(reviewRecordService).save(any(ReviewRecord.class));

            ReviewDTO dto = buildDTO(ReviewStatus.APPROVED.getCode(), null);

            reviewRecordService.doReview(PROFILE_ID, REVIEWER_ID, REVIEWER_NAME, dto);

            verify(reviewRecordService, times(1)).save(any(ReviewRecord.class));
        }
    }
}
