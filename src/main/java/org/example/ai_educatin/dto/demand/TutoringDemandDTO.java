package org.example.ai_educatin.dto.demand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 辅导需求 - 发布请求
 * 对标需求单格式:
 *   subject=数学,物理  teacherCount=1  currentLevel=补基础
 *   frequency=每周1次  durationHours=2  preferWeekday=周日
 *   pricePerHour=130  district=雁塔区  address=雁环路龙湖紫宸一期
 *   teacherGenderReq=1  teacherRequirement=高中理科经验丰富...
 */
@Data
public class TutoringDemandDTO {

    @NotNull(message = "请选择学生")
    private Long studentId;

    @NotBlank(message = "补习科目不能为空")
    private String subject;

    private Integer teacherCount = 1;

    private String currentLevel;

    @NotBlank(message = "补习频次不能为空")
    private String frequency;

    @NotNull(message = "每次时长不能为空")
    private BigDecimal durationHours;

    @NotBlank(message = "偏好上课日不能为空")
    private String preferWeekday;

    private String preferTimeSlot;

    @NotNull(message = "报价不能为空")
    private BigDecimal pricePerHour;

    @NotBlank(message = "区域不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    private Integer teachMode = 1;

    /** 老师性别要求: 1-男 2-女 null-不限 */
    private Integer teacherGenderReq;

    /** 其他要求 */
    private String teacherRequirement;
}
