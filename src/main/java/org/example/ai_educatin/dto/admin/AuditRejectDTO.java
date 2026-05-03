package org.example.ai_educatin.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审核拒绝请求体
 */
@Data
public class AuditRejectDTO {

    @NotBlank(message = "拒绝原因不能为空")
    private String reason;
}
