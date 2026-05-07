package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生信息表 - 一个家长可有多个孩子
 */
@Data
@TableName("student_info")
public class StudentInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 parent_profile.id */
    private Long parentId;

    /** 孩子姓名 */
    private String name;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 当前年级: 高一/初三/六年级... */
    private String grade;

    /** 就读学校 */
    private String school;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
