package org.example.ai_educatin.service.impl;

import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.entity.dto.student.StudentProfileDTO;
import org.example.ai_educatin.entity.dto.student.SupplementDTO;
import org.example.ai_educatin.mapper.StudentProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProfileServiceImpl 单元测试")
class StudentProfileServiceImplTest {

    @Spy
    @InjectMocks
    private StudentProfileServiceImpl studentProfileService;

    @Mock
    private StudentProfileMapper studentProfileMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
        lenient().when(redisTemplate.expireAt(anyString(), any(java.util.Date.class))).thenReturn(true);
    }

    // ==================== Helper Methods ====================

    private StudentProfileDTO buildValidDto() {
        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setRealName("张三");
        dto.setGender(1);
        dto.setBirthDate("2002-05-15");
        dto.setCity("北京-海淀-中关村");
        dto.setSchoolName("北京大学");
        dto.setGrade("JUNIOR");
        dto.setAvatar("https://example.com/avatar.jpg");
        dto.setSubjects("数学,物理");
        dto.setTags("耐心,有经验");
        dto.setIntroduction("热爱教学");
        return dto;
    }

    private StudentProfile buildExistingProfile(ReviewStatus status) {
        StudentProfile profile = new StudentProfile();
        profile.setId(1L);
        profile.setUserId(USER_ID);
        profile.setReviewStatus(status.getCode());
        profile.setRealName("张三");
        profile.setStudentNo("");
        return profile;
    }

    private StudentProfile buildExistingProfileWithStudentNo(ReviewStatus status, String studentNo) {
        StudentProfile profile = buildExistingProfile(status);
        profile.setStudentNo(studentNo);
        return profile;
    }

    // ==================== saveDraft Tests ====================

    @Nested
    @DisplayName("saveDraft - 保存草稿")
    class SaveDraftTests {

        @Test
        @DisplayName("SP-D-001: 首次保存，无已有档案 → 创建新档案，状态为DRAFT")
        void shouldCreateNewProfileWhenNoExisting() {
            // given
            doReturn(null).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).save(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.saveDraft(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.DRAFT.getCode());
            assertThat(result.getRealName()).isEqualTo("张三");
            assertThat(result.getStudentNo()).isEmpty();
            verify(studentProfileService).save(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-D-002: 已有DRAFT档案 → 更新成功")
        void shouldUpdateWhenExistingDraft() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.DRAFT);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();
            dto.setRealName("李四");

            // when
            StudentProfile result = studentProfileService.saveDraft(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getRealName()).isEqualTo("李四");
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-D-003: REJECTED档案 → 允许编辑（重新草稿）")
        void shouldAllowEditWhenRejected() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.REJECTED);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.saveDraft(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-D-004: PENDING_REVIEW档案 → 400 当前状态不允许编辑")
        void shouldThrowWhenPendingReview() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.PENDING_REVIEW);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            StudentProfileDTO dto = buildValidDto();

            // when & then
            assertThatThrownBy(() -> studentProfileService.saveDraft(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许编辑");
                    });
        }

        @Test
        @DisplayName("SP-D-005: APPROVED档案 → 400 当前状态不允许编辑")
        void shouldThrowWhenApproved() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.APPROVED);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            StudentProfileDTO dto = buildValidDto();

            // when & then
            assertThatThrownBy(() -> studentProfileService.saveDraft(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许编辑");
                    });
        }

        @Test
        @DisplayName("SP-D-006: NEED_SUPPLEMENT档案 → 400 当前状态不允许编辑")
        void shouldThrowWhenNeedSupplement() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.NEED_SUPPLEMENT);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            StudentProfileDTO dto = buildValidDto();

            // when & then
            assertThatThrownBy(() -> studentProfileService.saveDraft(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许编辑");
                    });
        }
    }

    // ==================== submitForReview Tests ====================

    @Nested
    @DisplayName("submitForReview - 提交审核")
    class SubmitForReviewTests {

        @Test
        @DisplayName("SP-R-001: DRAFT → PENDING_REVIEW，生成studentNo")
        void shouldTransitionDraftToPendingAndGenerateStudentNo() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.DRAFT);
            existing.setStudentNo("");
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.submitForReview(USER_ID, dto);

            // then
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW.getCode());
            assertThat(result.getStudentNo()).startsWith("STU");
            assertThat(result.getStudentNo()).hasSize(15); // STU(3) + yyyyMMdd(8) + 4位流水
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-R-002: REJECTED → PENDING_REVIEW")
        void shouldTransitionRejectedToPending() {
            // given
            StudentProfile existing = buildExistingProfileWithStudentNo(
                    ReviewStatus.REJECTED, "STU202401010001");
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.submitForReview(USER_ID, dto);

            // then
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW.getCode());
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-R-003: PENDING_REVIEW → 400 当前状态不允许提交审核")
        void shouldThrowWhenAlreadyPending() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.PENDING_REVIEW);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            StudentProfileDTO dto = buildValidDto();

            // when & then
            assertThatThrownBy(() -> studentProfileService.submitForReview(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许提交审核");
                    });
        }

        @Test
        @DisplayName("SP-R-004: APPROVED → 400 当前状态不允许提交审核")
        void shouldThrowWhenApproved() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.APPROVED);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            StudentProfileDTO dto = buildValidDto();

            // when & then
            assertThatThrownBy(() -> studentProfileService.submitForReview(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许提交审核");
                    });
        }

        @Test
        @DisplayName("SP-R-005: 无已有档案 → 创建新档案并生成studentNo")
        void shouldCreateNewAndGenerateStudentNoWhenNoExisting() {
            // given
            doReturn(null).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).save(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.submitForReview(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW.getCode());
            assertThat(result.getStudentNo()).startsWith("STU");
            verify(studentProfileService).save(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-R-006: 已有studentNo → 保持不变")
        void shouldKeepExistingStudentNo() {
            // given
            String existingNo = "STU202401010001";
            StudentProfile existing = buildExistingProfileWithStudentNo(ReviewStatus.DRAFT, existingNo);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));
            StudentProfileDTO dto = buildValidDto();

            // when
            StudentProfile result = studentProfileService.submitForReview(USER_ID, dto);

            // then
            assertThat(result.getStudentNo()).isEqualTo(existingNo);
            verify(valueOperations, never()).increment(anyString());
        }
    }

    // ==================== supplement Tests ====================

    @Nested
    @DisplayName("supplement - 补充材料")
    class SupplementTests {

        @Test
        @DisplayName("SP-SUP-001: NEED_SUPPLEMENT → PENDING_REVIEW，supplements字段更新")
        void shouldTransitionToPendingAndUpdateSupplements() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.NEED_SUPPLEMENT);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));

            SupplementDTO dto = new SupplementDTO();
            dto.setSupplements("https://example.com/supplement1.pdf");
            dto.setStudentIdPhoto("https://example.com/id_photo.jpg");

            // when
            StudentProfile result = studentProfileService.supplement(USER_ID, dto);

            // then
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW.getCode());
            assertThat(result.getSupplements()).isEqualTo("https://example.com/supplement1.pdf");
            assertThat(result.getStudentIdPhoto()).isEqualTo("https://example.com/id_photo.jpg");
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }

        @Test
        @DisplayName("SP-SUP-002: 档案不存在 → 404 学生档案不存在")
        void shouldThrow404WhenProfileNotFound() {
            // given
            doReturn(null).when(studentProfileService).getByUserId(USER_ID);
            SupplementDTO dto = new SupplementDTO();
            dto.setSupplements("https://example.com/supplement.pdf");

            // when & then
            assertThatThrownBy(() -> studentProfileService.supplement(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(404);
                        assertThat(bex.getMessage()).isEqualTo("学生档案不存在");
                    });
        }

        @Test
        @DisplayName("SP-SUP-003: DRAFT状态 → 400 当前状态不允许补充材料")
        void shouldThrowWhenDraft() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.DRAFT);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            SupplementDTO dto = new SupplementDTO();
            dto.setSupplements("https://example.com/supplement.pdf");

            // when & then
            assertThatThrownBy(() -> studentProfileService.supplement(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许补充材料");
                    });
        }

        @Test
        @DisplayName("SP-SUP-004: APPROVED状态 → 400 当前状态不允许补充材料")
        void shouldThrowWhenApproved() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.APPROVED);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            SupplementDTO dto = new SupplementDTO();
            dto.setSupplements("https://example.com/supplement.pdf");

            // when & then
            assertThatThrownBy(() -> studentProfileService.supplement(USER_ID, dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("当前状态不允许补充材料");
                    });
        }

        @Test
        @DisplayName("SP-SUP-005: 所有补充字段为空/null → 状态仍变为PENDING_REVIEW")
        void shouldStillTransitionWhenAllFieldsEmpty() {
            // given
            StudentProfile existing = buildExistingProfile(ReviewStatus.NEED_SUPPLEMENT);
            doReturn(existing).when(studentProfileService).getByUserId(USER_ID);
            doReturn(true).when(studentProfileService).updateById(any(StudentProfile.class));

            SupplementDTO dto = new SupplementDTO();
            // all fields null

            // when
            StudentProfile result = studentProfileService.supplement(USER_ID, dto);

            // then
            assertThat(result.getReviewStatus()).isEqualTo(ReviewStatus.PENDING_REVIEW.getCode());
            verify(studentProfileService).updateById(any(StudentProfile.class));
        }
    }
}
