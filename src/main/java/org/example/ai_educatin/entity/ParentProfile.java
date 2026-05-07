package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家长档案表
 */
@Data
@TableName("parent_profile")
public class ParentProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    /** 家长姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 所在区: 雁塔区/碑林区... */
    private String district;

    /** 详细地址 */
    private String address;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
