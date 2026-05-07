package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询工单表 - 家长端和学生端共用，通过 user_type 区分
 */
@Data
@TableName("ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工单编号(TKT前缀) */
    private String ticketNo;

    /** 提交人 user.id */
    private Long userId;

    /** 用户类型: 1-家长(PARENT) 2-学生(STUDENT) */
    private Integer userType;

    /** 关联需求 demand.id（可选） */
    private Long relatedDemandId;

    /** 咨询类型 */
    private String ticketType;

    /** 问题描述(限500字) */
    private String description;

    /** 联系电话 */
    private String contactPhone;

    /** 附件URL(逗号分隔,最多3个) */
    private String attachments;

    /** 工单状态: 0-PENDING 1-PROCESSING 2-RESOLVED 3-CLOSED */
    private Integer status;

    /** 处理人 user.id（客服） */
    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
