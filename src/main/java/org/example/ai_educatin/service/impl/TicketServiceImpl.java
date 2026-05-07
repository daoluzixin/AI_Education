package org.example.ai_educatin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.TicketStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.ticket.TicketCreateDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketQueryDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketReplyDTO;
import org.example.ai_educatin.entity.Ticket;
import org.example.ai_educatin.entity.TicketReply;
import org.example.ai_educatin.mapper.TicketMapper;
import org.example.ai_educatin.mapper.TicketReplyMapper;
import org.example.ai_educatin.service.TicketService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    private final TicketReplyMapper ticketReplyMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Ticket createTicket(Long userId, Integer userType, TicketCreateDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setUserId(userId);
        ticket.setUserType(userType);
        ticket.setTicketType(dto.getTicketType());
        ticket.setDescription(dto.getDescription());
        ticket.setContactPhone(dto.getContactPhone());
        ticket.setRelatedDemandId(dto.getRelatedDemandId());
        ticket.setAttachments(dto.getAttachments());
        ticket.setStatus(TicketStatus.PENDING.getCode());
        save(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Ticket>()
                .eq(Ticket::getUserId, userId)
                .orderByDesc(Ticket::getCreateTime));
    }

    @Override
    public IPage<Ticket> pageQuery(TicketQueryDTO dto) {
        Page<Ticket> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();

        if (dto.getUserType() != null) {
            wrapper.eq(Ticket::getUserType, dto.getUserType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Ticket::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getTicketType())) {
            wrapper.eq(Ticket::getTicketType, dto.getTicketType());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Ticket::getTicketNo, dto.getKeyword())
                    .or().like(Ticket::getContactPhone, dto.getKeyword()));
        }

        wrapper.orderByDesc(Ticket::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void acceptTicket(Long ticketId, Long handlerId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        TicketStatus current = TicketStatus.fromCode(ticket.getStatus());
        if (current != TicketStatus.PENDING) {
            throw new BusinessException(400, "只有待处理工单才能接单，当前状态: " + current.getDesc());
        }

        ticket.setStatus(TicketStatus.PROCESSING.getCode());
        ticket.setHandlerId(handlerId);
        updateById(ticket);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyTicket(Long ticketId, Long replierId, String replierName, TicketReplyDTO dto) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        TicketStatus current = TicketStatus.fromCode(ticket.getStatus());
        if (current == TicketStatus.CLOSED || current == TicketStatus.RESOLVED) {
            throw new BusinessException(400, "工单已关闭或已解决，无法回复");
        }

        // 写入回复
        TicketReply reply = new TicketReply();
        reply.setTicketId(ticketId);
        reply.setReplierId(replierId);
        reply.setReplierName(replierName);
        reply.setContent(dto.getContent());
        reply.setAttachments(dto.getAttachments());
        ticketReplyMapper.insert(reply);

        // 如果是待处理状态，自动接单并进入处理中
        if (current == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.PROCESSING.getCode());
            ticket.setHandlerId(replierId);
            updateById(ticket);
        }
    }

    @Override
    public void closeTicket(Long ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BusinessException(404, "工单不存在");
        }

        TicketStatus current = TicketStatus.fromCode(ticket.getStatus());
        if (current == TicketStatus.CLOSED) {
            throw new BusinessException(400, "工单已关闭");
        }

        ticket.setStatus(TicketStatus.CLOSED.getCode());
        updateById(ticket);
    }

    @Override
    public List<TicketReply> listReplies(Long ticketId) {
        return ticketReplyMapper.selectList(new LambdaQueryWrapper<TicketReply>()
                .eq(TicketReply::getTicketId, ticketId)
                .orderByAsc(TicketReply::getCreateTime));
    }

    /**
     * 生成工单编号: TKT + yyyyMMdd + 4位流水号
     */
    private String generateTicketNo() {
        String today = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        String key = "seq:TKT:" + today;
        Long seq = redisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1L) {
            redisTemplate.expireAt(key, java.util.Date.from(
                    LocalDate.now().plusDays(1).atStartOfDay()
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return String.format("TKT%s%04d", today, seq);
    }
}
