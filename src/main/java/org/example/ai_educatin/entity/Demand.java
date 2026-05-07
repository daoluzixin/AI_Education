package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家长需求表
 */
@Data
@TableName("demand")
public class Demand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 需求编号(REQ前缀) */
    private String demandNo;

    /** 关联 user.id（家长） */
    private Long userId;

    /** 孩子年级: PRIMARY_1~SENIOR_3 */
    private String childGrade;

    /** 需求类型: SUBJECT_TUTOR/INTEREST/COMPETITION/ADMISSION/OTHER */
    private String demandType;

    /** 城市(省-市-区) */
    private String city;

    /** 期望条件(限200字) */
    private String expectations;

    /** 预算范围: 50-100/100-150/150-200/200+ */
    private String budget;

    /** 补充说明(限500字) */
    private String remark;

    /** 需求状态: 0-PENDING 1-MATCHING 2-RECOMMENDED 3-CLOSED */
    private Integer status;

    /** 关闭原因(CLOSED时填写) */
    private String closeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
