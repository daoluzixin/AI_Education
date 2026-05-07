package org.example.ai_educatin.entity.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 后台配置推荐请求
 */
@Data
public class RecommendDTO {

    @NotNull(message = "需求ID不能为空")
    private Long demandId;

    /** 推荐的学生ID列表（最多5个，按顺序） */
    @NotEmpty(message = "推荐学生列表不能为空")
    @Size(max = 5, message = "每条需求最多推荐5位学生")
    private List<Long> studentIds;
}
