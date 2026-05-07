package org.example.ai_educatin.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学生信息 - 新增/更新请求
 */
@Data
public class StudentInfoDTO {

    @NotBlank(message = "孩子姓名不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotBlank(message = "年级不能为空")
    private String grade;

    private String school;

    private String remark;
}
