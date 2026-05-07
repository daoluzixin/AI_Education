package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单回复表 - 客服回复记录，一个工单可有多条回复
 */
@Data
@TableName("ticket_reply")
public class TicketReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 ticket.id */
    private Long ticketId;

    /** 回复人 user.id（客服） */
    private Long replierId;

    /** 回复人账号名 */
    private String replierName;

    /** 回复内容 */
    private String content;

    /** 附件URL(逗号分隔) */
    private String attachments;

    /** 回复时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
