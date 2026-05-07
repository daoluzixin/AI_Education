package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐反馈表 - 记录家长对推荐结果的行为，用于闭环优化
 */
@Data
@TableName("recommendation_feedback")
public class RecommendationFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 demand.id */
    private Long demandId;

    /** 关联 student_profile.id */
    private Long studentId;

    /** 反馈类型: 1-查看详情 2-发起咨询 3-确认选择 4-不满意 */
    private Integer feedbackType;

    /** 反馈备注 */
    private String feedbackNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
