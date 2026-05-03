package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师档案表
 */
@Data
@TableName("teacher_profile")
public class TeacherProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 user.id */
    private Long userId;

    // ---- 实名认证 ----
    private String realName;

    /** 性别: 1-男 2-女 */
    private Integer gender;

    /** 身份证号(AES加密存储) */
    private String idCardEncrypted;

    /** 身份证正面照片 */
    private String idCardFrontUrl;

    /** 身份证背面照片 */
    private String idCardBackUrl;

    // ---- 学籍信息 ----
    /** 就读大学 */
    private String university;

    /** 专业 */
    private String major;

    /** 学历层次: 本科/硕士/博士 */
    private String educationLevel;

    /** 在读年级: 大一~大四/研一~研三 */
    private String grade;

    /** 学生证照片 */
    private String studentIdUrl;

    // ---- 教学简历 ----
    /** 个人简介 */
    private String selfIntro;

    /** 家教经验描述 */
    private String teachingExperience;

    /** 擅长科目,逗号分隔 */
    private String subjects;

    /** 可教年级范围,逗号分隔 */
    private String gradeRange;

    // ---- 服务范围 ----
    /** 服务区域(区): 雁塔区/碑林区... */
    private String district;

    /** 常驻地址 */
    private String detailAddress;

    /** 授课方式: 1-上门 2-线上 3-均可 */
    private Integer teachMode;

    // ---- 价格 ----
    /** 期望时薪(元) */
    private BigDecimal pricePerHour;

    // ---- 审核 ----
    /** 认证状态: 0-待审核 1-已通过 2-已拒绝 */
    private Integer authStatus;

    /** 拒绝原因 */
    private String rejectReason;

    // ---- 统计 ----
    /** 平均评分 */
    private BigDecimal avgRating;

    /** 累计完成订单数 */
    private Integer totalOrders;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
