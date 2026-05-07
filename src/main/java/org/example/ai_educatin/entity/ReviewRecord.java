package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录表 - 每次审核操作生成一条，不可删除不可修改，形成审计轨迹
 */
@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 student_profile.id */
    private Long studentId;

    /** 审核人 user.id（管理员） */
    private Long reviewerId;

    /** 审核人账号名 */
    private String reviewerName;

    /** 审核结果: 2-APPROVED 3-REJECTED 4-NEED_SUPPLEMENT */
    private Integer reviewResult;

    /** 审核备注(驳回原因或补充说明) */
    private String reviewNote;

    /** 审核时间(创建后不可修改) */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
