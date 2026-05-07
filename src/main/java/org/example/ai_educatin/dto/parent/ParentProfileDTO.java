package org.example.ai_educatin.dto.parent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 家长档案 - 提交/更新请求
 */
@Data
public class ParentProfileDTO {

    private String realName;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "所在区不能为空")
    private String district;

    private String address;
}
