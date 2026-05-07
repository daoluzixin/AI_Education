package org.example.ai_educatin.entity.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台审核操作请求
 */
@Data
public class ReviewDTO {

    /** 审核结果: 2-通过 3-驳回 4-要求补充 */
    @NotNull(message = "审核结果不能为空")
    private Integer reviewResult;

    /** 审核备注（驳回原因或补充说明，驳回/补充时必填） */
    private String reviewNote;
}
