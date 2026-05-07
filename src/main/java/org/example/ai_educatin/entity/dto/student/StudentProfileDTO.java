package org.example.ai_educatin.entity.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生入驻资料填写/更新请求
 */
@Data
public class StudentProfileDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotBlank(message = "出生日期不能为空")
    private String birthDate;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "个人照片不能为空")
    private String avatar;

    @Size(max = 300, message = "自我介绍不超过300字")
    private String introduction;

    @NotBlank(message = "擅长方向不能为空")
    private String subjects;

    @NotBlank(message = "个人标签不能为空")
    private String tags;

    /** 学生证照片URL（正反面逗号分隔） */
    private String studentIdPhoto;

    /** 获奖证书URL（逗号分隔，最多5张） */
    private String certificates;

    /** 成绩证明URL（逗号分隔，最多3张） */
    private String transcripts;

    /** 其他补充材料URL */
    private String supplements;
}
