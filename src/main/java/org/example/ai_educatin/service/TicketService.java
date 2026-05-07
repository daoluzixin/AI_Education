package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.ticket.TicketCreateDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketQueryDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketReplyDTO;
import org.example.ai_educatin.entity.Ticket;
import org.example.ai_educatin.entity.TicketReply;

import java.util.List;

public interface TicketService extends IService<Ticket> {

    /**
     * 用户提交工单
     */
    Ticket createTicket(Long userId, Integer userType, TicketCreateDTO dto);

    /**
     * 用户查看自己的工单列表
     */
    List<Ticket> listByUserId(Long userId);

    /**
     * 后台分页查询工单
     */
    IPage<Ticket> pageQuery(TicketQueryDTO dto);

    /**
     * 客服接单
     */
    void acceptTicket(Long ticketId, Long handlerId);

    /**
     * 客服回复
     */
    void replyTicket(Long ticketId, Long replierId, String replierName, TicketReplyDTO dto);

    /**
     * 关闭工单
     */
    void closeTicket(Long ticketId);

    /**
     * 查看工单的所有回复
     */
    List<TicketReply> listReplies(Long ticketId);
}
