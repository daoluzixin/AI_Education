package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生档案表 - 大学生家教入驻资料，与 user 表一对一
 */
@Data
@TableName("student_profile")
public class StudentProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    /** 学生编号(STU前缀,提交审核时生成) */
    private String studentNo;

    /** 真实姓名 */
    private String realName;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 所在城市(省-市-区) */
    private String city;

    /** 学校名称 */
    private String schoolName;

    /** 年级: FRESHMAN/SOPHOMORE/JUNIOR/SENIOR/MASTER_1/MASTER_2/MASTER_3 */
    private String grade;

    /** 个人照片URL */
    private String avatar;

    /** 自我介绍(限300字) */
    private String introduction;

    /** 擅长方向(逗号分隔) */
    private String subjects;

    /** 个人标签(逗号分隔,最多5个) */
    private String tags;

    /** 学生证照片URL(正反面,逗号分隔) */
    private String studentIdPhoto;

    /** 获奖证书URL(逗号分隔,最多5张) */
    private String certificates;

    /** 成绩证明URL(逗号分隔,最多3张) */
    private String transcripts;

    /** 其他补充材料URL(逗号分隔,最多3个) */
    private String supplements;

    /** 期望时薪范围: 50-100/100-150/150-200/200+ */
    private String hourlyRate;

    /** 审核状态: 0-DRAFT 1-PENDING_REVIEW 2-APPROVED 3-REJECTED 4-NEED_SUPPLEMENT */
    private Integer reviewStatus;

    /** 驳回原因(REJECTED时填写) */
    private String rejectReason;

    /** 需补充内容说明(NEED_SUPPLEMENT时填写) */
    private String supplementNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
