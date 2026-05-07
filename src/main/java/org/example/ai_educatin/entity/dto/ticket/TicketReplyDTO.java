package org.example.ai_educatin.entity.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 客服回复工单请求
 */
@Data
public class TicketReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    private String content;

    /** 附件URL（逗号分隔） */
    private String attachments;
}
