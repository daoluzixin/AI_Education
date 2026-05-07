package org.example.ai_educatin.entity.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交咨询工单请求
 */
@Data
public class TicketCreateDTO {

    @NotBlank(message = "咨询类型不能为空")
    private String ticketType;

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 500, message = "问题描述不超过500字")
    private String description;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /** 关联需求ID（可选） */
    private Long relatedDemandId;

    /** 附件URL（逗号分隔，最多3个） */
    private String attachments;
}
