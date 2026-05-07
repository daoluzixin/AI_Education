package org.example.ai_educatin.service.impl;

import org.example.ai_educatin.common.enums.TicketStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.Ticket;
import org.example.ai_educatin.entity.TicketReply;
import org.example.ai_educatin.entity.dto.ticket.TicketCreateDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketReplyDTO;
import org.example.ai_educatin.mapper.TicketMapper;
import org.example.ai_educatin.mapper.TicketReplyMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * TicketServiceImpl 单元测试
 *
 * 核心验证：工单创建 + 状态流转合法性
 * 状态机规则：
 * - PENDING(0) -> PROCESSING(1)（接单/回复自动接单）
 * - PENDING/PROCESSING/RESOLVED -> CLOSED（关闭）
 * - CLOSED 不可再次关闭
 * - CLOSED/RESOLVED 不可回复
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketReplyMapper ticketReplyMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    @InjectMocks
    private TicketServiceImpl ticketService;

    private static final Long USER_ID = 10L;
    private static final Long HANDLER_ID = 50L;
    private static final Long TICKET_ID = 1L;
    private static final String REPLIER_NAME = "客服小王";

    @BeforeEach
    void setUp() {
        // Redis mock for generateTicketNo - lenient since not all tests call createTicket
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
    }

    private Ticket buildTicket(Long id, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setStatus(status.getCode());
        ticket.setUserId(USER_ID);
        ticket.setTicketNo("TKT202605070001");
        return ticket;
    }

    private TicketCreateDTO buildCreateDTO(String ticketType, String description, String contactPhone,
                                           Long relatedDemandId, String attachments) {
        TicketCreateDTO dto = new TicketCreateDTO();
        dto.setTicketType(ticketType);
        dto.setDescription(description);
        dto.setContactPhone(contactPhone);
        dto.setRelatedDemandId(relatedDemandId);
        dto.setAttachments(attachments);
        return dto;
    }

    private TicketReplyDTO buildReplyDTO(String content, String attachments) {
        TicketReplyDTO dto = new TicketReplyDTO();
        dto.setContent(content);
        dto.setAttachments(attachments);
        return dto;
    }

    // ==================== createTicket ====================

    @Nested
    @DisplayName("createTicket 创建工单")
    class CreateTicketCases {

        @Test
        @DisplayName("T-C-001: 家长(userType=1)创建工单 -> 状态为 PENDING，ticketNo 已生成")
        void parentCreateTicket_success() {
            doReturn(true).when(ticketService).save(any(Ticket.class));

            TicketCreateDTO dto = buildCreateDTO("课程咨询", "想找数学家教", "13800138000", 1L, "http://img.com/a.jpg");

            Ticket result = ticketService.createTicket(USER_ID, 1, dto);

            assertNotNull(result);
            assertEquals(TicketStatus.PENDING.getCode(), result.getStatus());
            assertNotNull(result.getTicketNo());
            assertTrue(result.getTicketNo().startsWith("TKT"));
            assertEquals(1, result.getUserType());
            verify(ticketService, times(1)).save(any(Ticket.class));
        }

        @Test
        @DisplayName("T-C-002: 学生(userType=2)创建工单 -> 成功")
        void studentCreateTicket_success() {
            doReturn(true).when(ticketService).save(any(Ticket.class));

            TicketCreateDTO dto = buildCreateDTO("提现问题", "提现失败", "13900139000", null, null);

            Ticket result = ticketService.createTicket(USER_ID, 2, dto);

            assertNotNull(result);
            assertEquals(TicketStatus.PENDING.getCode(), result.getStatus());
            assertEquals(2, result.getUserType());
        }

        @Test
        @DisplayName("T-C-003: relatedDemandId=null -> 成功创建")
        void createTicket_nullRelatedDemandId_success() {
            doReturn(true).when(ticketService).save(any(Ticket.class));

            TicketCreateDTO dto = buildCreateDTO("其他", "一般咨询", "13700137000", null, "http://img.com/b.jpg");

            Ticket result = ticketService.createTicket(USER_ID, 1, dto);

            assertNotNull(result);
            assertNull(result.getRelatedDemandId());
        }

        @Test
        @DisplayName("T-C-004: attachments=null -> 成功创建")
        void createTicket_nullAttachments_success() {
            doReturn(true).when(ticketService).save(any(Ticket.class));

            TicketCreateDTO dto = buildCreateDTO("课程咨询", "想了解服务", "13600136000", 2L, null);

            Ticket result = ticketService.createTicket(USER_ID, 1, dto);

            assertNotNull(result);
            assertNull(result.getAttachments());
        }
    }

    // ==================== acceptTicket ====================

    @Nested
    @DisplayName("acceptTicket 接单")
    class AcceptTicketCases {

        @Test
        @DisplayName("T-A-001: PENDING 工单 -> 状态变为 PROCESSING，handlerId 被设置")
        void pendingTicket_acceptSuccess() {
            Ticket pendingTicket = buildTicket(TICKET_ID, TicketStatus.PENDING);
            doReturn(pendingTicket).when(ticketService).getById(TICKET_ID);
            doReturn(true).when(ticketService).updateById(any(Ticket.class));

            assertDoesNotThrow(() -> ticketService.acceptTicket(TICKET_ID, HANDLER_ID));
            assertEquals(TicketStatus.PROCESSING.getCode(), pendingTicket.getStatus());
            assertEquals(HANDLER_ID, pendingTicket.getHandlerId());
        }

        @Test
        @DisplayName("T-A-002: ticketId 不存在 -> 404 工单不存在")
        void ticketNotFound_throws404() {
            doReturn(null).when(ticketService).getById(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.acceptTicket(999L, HANDLER_ID));
            assertEquals(404, ex.getCode());
            assertTrue(ex.getMessage().contains("工单不存在"));
        }

        @Test
        @DisplayName("T-A-003: PROCESSING 工单 -> 400 只有待处理工单才能接单")
        void processingTicket_throws400() {
            Ticket processingTicket = buildTicket(TICKET_ID, TicketStatus.PROCESSING);
            doReturn(processingTicket).when(ticketService).getById(TICKET_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.acceptTicket(TICKET_ID, HANDLER_ID));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("只有待处理工单才能接单"));
        }

        @Test
        @DisplayName("T-A-004: CLOSED 工单 -> 400")
        void closedTicket_throws400() {
            Ticket closedTicket = buildTicket(TICKET_ID, TicketStatus.CLOSED);
            doReturn(closedTicket).when(ticketService).getById(TICKET_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.acceptTicket(TICKET_ID, HANDLER_ID));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("T-A-005: RESOLVED 工单 -> 400")
        void resolvedTicket_throws400() {
            Ticket resolvedTicket = buildTicket(TICKET_ID, TicketStatus.RESOLVED);
            doReturn(resolvedTicket).when(ticketService).getById(TICKET_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.acceptTicket(TICKET_ID, HANDLER_ID));
            assertEquals(400, ex.getCode());
        }
    }

    // ==================== replyTicket ====================

    @Nested
    @DisplayName("replyTicket 回复工单")
    class ReplyTicketCases {

        @Test
        @DisplayName("T-R-001: PROCESSING 工单 -> 成功添加 TicketReply")
        void processingTicket_replySuccess() {
            Ticket processingTicket = buildTicket(TICKET_ID, TicketStatus.PROCESSING);
            doReturn(processingTicket).when(ticketService).getById(TICKET_ID);
            when(ticketReplyMapper.insert(any(TicketReply.class))).thenReturn(1);

            TicketReplyDTO dto = buildReplyDTO("已处理您的问题", null);

            assertDoesNotThrow(() ->
                    ticketService.replyTicket(TICKET_ID, HANDLER_ID, REPLIER_NAME, dto));
            verify(ticketReplyMapper, times(1)).insert(any(TicketReply.class));
            // 状态保持 PROCESSING，不变
            assertEquals(TicketStatus.PROCESSING.getCode(), processingTicket.getStatus());
        }

        @Test
        @DisplayName("T-R-002: PENDING 工单 -> 自动接单(->PROCESSING) + 添加回复")
        void pendingTicket_autoAcceptAndReply() {
            Ticket pendingTicket = buildTicket(TICKET_ID, TicketStatus.PENDING);
            doReturn(pendingTicket).when(ticketService).getById(TICKET_ID);
            when(ticketReplyMapper.insert(any(TicketReply.class))).thenReturn(1);
            doReturn(true).when(ticketService).updateById(any(Ticket.class));

            TicketReplyDTO dto = buildReplyDTO("您好，我来处理", "http://img.com/reply.jpg");

            assertDoesNotThrow(() ->
                    ticketService.replyTicket(TICKET_ID, HANDLER_ID, REPLIER_NAME, dto));
            assertEquals(TicketStatus.PROCESSING.getCode(), pendingTicket.getStatus());
            assertEquals(HANDLER_ID, pendingTicket.getHandlerId());
            verify(ticketReplyMapper, times(1)).insert(any(TicketReply.class));
        }

        @Test
        @DisplayName("T-R-003: ticketId 不存在 -> 404 工单不存在")
        void ticketNotFound_throws404() {
            doReturn(null).when(ticketService).getById(999L);

            TicketReplyDTO dto = buildReplyDTO("回复内容", null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.replyTicket(999L, HANDLER_ID, REPLIER_NAME, dto));
            assertEquals(404, ex.getCode());
            assertTrue(ex.getMessage().contains("工单不存在"));
        }

        @Test
        @DisplayName("T-R-004: CLOSED 工单 -> 400 工单已关闭或已解决")
        void closedTicket_throws400() {
            Ticket closedTicket = buildTicket(TICKET_ID, TicketStatus.CLOSED);
            doReturn(closedTicket).when(ticketService).getById(TICKET_ID);

            TicketReplyDTO dto = buildReplyDTO("尝试回复", null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.replyTicket(TICKET_ID, HANDLER_ID, REPLIER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("工单已关闭或已解决"));
        }

        @Test
        @DisplayName("T-R-005: RESOLVED 工单 -> 400 工单已关闭或已解决")
        void resolvedTicket_throws400() {
            Ticket resolvedTicket = buildTicket(TICKET_ID, TicketStatus.RESOLVED);
            doReturn(resolvedTicket).when(ticketService).getById(TICKET_ID);

            TicketReplyDTO dto = buildReplyDTO("尝试回复", null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.replyTicket(TICKET_ID, HANDLER_ID, REPLIER_NAME, dto));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("工单已关闭或已解决"));
        }
    }

    // ==================== closeTicket ====================

    @Nested
    @DisplayName("closeTicket 关闭工单")
    class CloseTicketCases {

        @Test
        @DisplayName("T-CL-001: PENDING -> CLOSED")
        void pendingTicket_closeSuccess() {
            Ticket pendingTicket = buildTicket(TICKET_ID, TicketStatus.PENDING);
            doReturn(pendingTicket).when(ticketService).getById(TICKET_ID);
            doReturn(true).when(ticketService).updateById(any(Ticket.class));

            assertDoesNotThrow(() -> ticketService.closeTicket(TICKET_ID));
            assertEquals(TicketStatus.CLOSED.getCode(), pendingTicket.getStatus());
        }

        @Test
        @DisplayName("T-CL-002: PROCESSING -> CLOSED")
        void processingTicket_closeSuccess() {
            Ticket processingTicket = buildTicket(TICKET_ID, TicketStatus.PROCESSING);
            doReturn(processingTicket).when(ticketService).getById(TICKET_ID);
            doReturn(true).when(ticketService).updateById(any(Ticket.class));

            assertDoesNotThrow(() -> ticketService.closeTicket(TICKET_ID));
            assertEquals(TicketStatus.CLOSED.getCode(), processingTicket.getStatus());
        }

        @Test
        @DisplayName("T-CL-003: ticketId 不存在 -> 404 工单不存在")
        void ticketNotFound_throws404() {
            doReturn(null).when(ticketService).getById(999L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.closeTicket(999L));
            assertEquals(404, ex.getCode());
            assertTrue(ex.getMessage().contains("工单不存在"));
        }

        @Test
        @DisplayName("T-CL-004: CLOSED -> CLOSED -> 400 工单已关闭")
        void closedTicket_throws400() {
            Ticket closedTicket = buildTicket(TICKET_ID, TicketStatus.CLOSED);
            doReturn(closedTicket).when(ticketService).getById(TICKET_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ticketService.closeTicket(TICKET_ID));
            assertEquals(400, ex.getCode());
            assertTrue(ex.getMessage().contains("工单已关闭"));
        }

        @Test
        @DisplayName("T-CL-005: RESOLVED -> CLOSED（允许关闭已解决工单）")
        void resolvedTicket_closeSuccess() {
            Ticket resolvedTicket = buildTicket(TICKET_ID, TicketStatus.RESOLVED);
            doReturn(resolvedTicket).when(ticketService).getById(TICKET_ID);
            doReturn(true).when(ticketService).updateById(any(Ticket.class));

            assertDoesNotThrow(() -> ticketService.closeTicket(TICKET_ID));
            assertEquals(TicketStatus.CLOSED.getCode(), resolvedTicket.getStatus());
        }
    }
}
