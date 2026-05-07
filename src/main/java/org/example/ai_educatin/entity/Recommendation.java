package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐关系表 - 需求与学生的多对多关系，每条需求最多5位学生
 */
@Data
@TableName("recommendation")
public class Recommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 demand.id */
    private Long demandId;

    /** 关联 student_profile.id */
    private Long studentId;

    /** 推荐顺序(越小越靠前) */
    private Integer sortOrder;

    /** 操作人 user.id（运营） */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
