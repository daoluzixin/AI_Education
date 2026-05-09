package org.example.ai_educatin.service.impl;

import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.mapper.DemandMapper;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.service.MatchingService;
import org.example.ai_educatin.vo.CandidateVO;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DemandServiceImpl 对抗测试
 *
 * 核心验证：需求状态流转合法性（对标 03-constraints.md 第4节）
 * 状态机规则：
 * - PENDING → MATCHING(瞬时) → RECOMMENDED（自动匹配成功）
 * - PENDING / MATCHING → 可触发匹配（MATCHING 用于重试场景）
 * - 任意状态 → CLOSED（唯一例外）
 * - 已 CLOSED 不可再次关闭
 * - RECOMMENDED / CLOSED 不可开始匹配
 *
 * 注意：DemandServiceImpl 继承 ServiceImpl，getById/updateById 走的是 baseMapper。
 * 使用 @Spy 让实际逻辑执行，同时 mock 掉 baseMapper 的数据库操作。
 */
@ExtendWith(MockitoExtension.class)
class DemandServiceImplTest {

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MatchingService matchingService;

    @Mock
    private RecommendationMapper recommendationMapper;

    @Spy
    @InjectMocks
    private DemandServiceImpl demandService;

    private Demand pendingDemand;
    private Demand matchingDemand;
    private Demand recommendedDemand;
    private Demand closedDemand;

    @BeforeEach
    void setUp() {
        pendingDemand = buildDemand(1L, DemandStatus.PENDING);
        matchingDemand = buildDemand(2L, DemandStatus.MATCHING);
        recommendedDemand = buildDemand(3L, DemandStatus.RECOMMENDED);
        closedDemand = buildDemand(4L, DemandStatus.CLOSED);
    }

    private Demand buildDemand(Long id, DemandStatus status) {
        Demand d = new Demand();
        d.setId(id);
        d.setStatus(status.getCode());
        d.setDemandNo("REQ2026050700" + id);
        return d;
    }

    // ==================== startMatching 状态流转 ====================

    @Nested
    @DisplayName("startMatching 状态流转")
    class StartMatchingCases {

        @Test
        @DisplayName("PENDING → 自动匹配成功 → RECOMMENDED")
        void pending_autoMatch_success() {
            doReturn(pendingDemand).when(demandService).getById(1L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            CandidateVO candidate = new CandidateVO();
            candidate.setStudentId(100L);
            when(matchingService.findCandidates(eq(1L), anyInt()))
                    .thenReturn(List.of(candidate));
            when(recommendationMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> demandService.startMatching(1L));
            assertEquals(DemandStatus.RECOMMENDED.getCode(), pendingDemand.getStatus());
        }

        @Test
        @DisplayName("PENDING → 自动匹配无候选 → 保持 MATCHING")
        void pending_autoMatch_noCandidates_stayMatching() {
            doReturn(pendingDemand).when(demandService).getById(1L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            when(matchingService.findCandidates(eq(1L), anyInt()))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> demandService.startMatching(1L));
            assertEquals(DemandStatus.MATCHING.getCode(), pendingDemand.getStatus());
        }

        @Test
        @DisplayName("MATCHING → 重试匹配：合法（无候选时可重试）")
        void matching_retry_success() {
            doReturn(matchingDemand).when(demandService).getById(2L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            CandidateVO candidate = new CandidateVO();
            candidate.setStudentId(200L);
            when(matchingService.findCandidates(eq(2L), anyInt()))
                    .thenReturn(List.of(candidate));
            when(recommendationMapper.delete(any())).thenReturn(0);
            when(recommendationMapper.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> demandService.startMatching(2L));
            assertEquals(DemandStatus.RECOMMENDED.getCode(), matchingDemand.getStatus());
        }

        @Test
        @DisplayName("RECOMMENDED → MATCHING：非法（不可回退）")
        void recommended_to_matching_throws() {
            doReturn(recommendedDemand).when(demandService).getById(3L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> demandService.startMatching(3L));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("CLOSED → MATCHING：非法（已关闭不可重启）")
        void closed_to_matching_throws() {
            doReturn(closedDemand).when(demandService).getById(4L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> demandService.startMatching(4L));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("需求不存在 → 抛 404")
        void demandNotFound_throws404() {
            doReturn(null).when(demandService).getById(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> demandService.startMatching(999L));
            assertEquals(404, ex.getCode());
        }
    }

    // ==================== closeDemand 状态流转 ====================

    @Nested
    @DisplayName("closeDemand 状态流转")
    class CloseDemandCases {

        @Test
        @DisplayName("PENDING → CLOSED：合法（任意状态可关闭）")
        void pending_to_closed_success() {
            doReturn(pendingDemand).when(demandService).getById(1L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            assertDoesNotThrow(() -> demandService.closeDemand(1L, "不需要了"));
            assertEquals(DemandStatus.CLOSED.getCode(), pendingDemand.getStatus());
            assertEquals("不需要了", pendingDemand.getCloseReason());
        }

        @Test
        @DisplayName("MATCHING → CLOSED：合法")
        void matching_to_closed_success() {
            doReturn(matchingDemand).when(demandService).getById(2L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            assertDoesNotThrow(() -> demandService.closeDemand(2L, "已找到家教"));
            assertEquals(DemandStatus.CLOSED.getCode(), matchingDemand.getStatus());
        }

        @Test
        @DisplayName("RECOMMENDED → CLOSED：合法")
        void recommended_to_closed_success() {
            doReturn(recommendedDemand).when(demandService).getById(3L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            assertDoesNotThrow(() -> demandService.closeDemand(3L, null));
            assertEquals(DemandStatus.CLOSED.getCode(), recommendedDemand.getStatus());
        }

        @Test
        @DisplayName("CLOSED → CLOSED：非法（不可重复关闭）")
        void closed_to_closed_throws() {
            doReturn(closedDemand).when(demandService).getById(4L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> demandService.closeDemand(4L, "再次关闭"));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("已关闭"));
        }

        @Test
        @DisplayName("需求不存在 → 抛 404")
        void demandNotFound_throws404() {
            doReturn(null).when(demandService).getById(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> demandService.closeDemand(999L, "不存在"));
            assertEquals(404, ex.getCode());
        }

        @Test
        @DisplayName("closeReason 为 null → 正常关闭（reason 可选）")
        void nullReason_closesNormally() {
            doReturn(pendingDemand).when(demandService).getById(1L);
            doReturn(true).when(demandService).updateById(any(Demand.class));

            assertDoesNotThrow(() -> demandService.closeDemand(1L, null));
            assertEquals(DemandStatus.CLOSED.getCode(), pendingDemand.getStatus());
            assertNull(pendingDemand.getCloseReason());
        }
    }
}
