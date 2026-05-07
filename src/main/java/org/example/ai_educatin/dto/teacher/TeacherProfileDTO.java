package org.example.ai_educatin.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师档案 - 提交/更新请求
 */
@Data
public class TeacherProfileDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotBlank(message = "大学名称不能为空")
    private String university;

    private String major;

    @NotBlank(message = "学历层次不能为空")
    private String educationLevel;

    @NotBlank(message = "在读年级不能为空")
    private String grade;

    private String selfIntro;

    private String teachingExperience;

    @NotBlank(message = "擅长科目不能为空")
    private String subjects;

    @NotBlank(message = "可教年级不能为空")
    private String gradeRange;

    @NotBlank(message = "服务区域不能为空")
    private String district;

    private String detailAddress;

    @NotNull(message = "授课方式不能为空")
    private Integer teachMode;

    @NotNull(message = "期望时薪不能为空")
    private BigDecimal pricePerHour;
}
