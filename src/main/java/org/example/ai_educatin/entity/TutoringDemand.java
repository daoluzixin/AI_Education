package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 辅导需求表 - 对应家长发布的家教需求单
 *
 * 字段映射示例 (043043号家教):
 *   demandNo       ← 043043
 *   subject        ← 数学,物理
 *   teacherCount   ← 1
 *   currentLevel   ← 补基础
 *   frequency      ← 每周1次
 *   durationHours  ← 2.0
 *   preferWeekday  ← 周日
 *   pricePerHour   ← 130.00
 *   district       ← 雁塔区
 *   address        ← 雁环路龙湖紫宸一期
 *   teacherGenderReq ← 1(男)
 *   teacherRequirement ← 高中理科经验丰富，思路清晰有方法
 */
@Data
@TableName("tutoring_demand")
public class TutoringDemand {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 需求编号 */
    private String demandNo;

    /** 关联 parent_profile.id */
    private Long parentId;

    /** 关联 student_info.id */
    private Long studentId;

    // ---- 科目与成绩 ----
    /** 补习科目(逗号分隔) */
    private String subject;

    /** 需要几位老师 */
    private Integer teacherCount;

    /** 现阶段水平: 补基础/中等提升/拔尖冲刺 */
    private String currentLevel;

    // ---- 时间安排 ----
    /** 频次: 每周1次/每周2次 */
    private String frequency;

    /** 每次时长(小时) */
    private BigDecimal durationHours;

    /** 偏好上课日: 周日 / 周六,周日 */
    private String preferWeekday;

    /** 偏好时间段: 上午/下午/晚上/不限 */
    private String preferTimeSlot;

    // ---- 价格 ----
    /** 报价(元/小时) */
    private BigDecimal pricePerHour;

    // ---- 地点 ----
    /** 区: 雁塔区 */
    private String district;

    /** 详细地址 */
    private String address;

    /** 授课方式: 1-上门 2-线上 3-均可 */
    private Integer teachMode;

    // ---- 对老师的要求 ----
    /** 老师性别要求: 1-男 2-女 null-不限 */
    private Integer teacherGenderReq;

    /** 其他要求(自由文本) */
    private String teacherRequirement;

    // ---- 状态 ----
    /** 0-发布中 1-已匹配 2-上课中 3-已完成 4-已关闭 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
