package org.example.ai_educatin.entity.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求（手机号/账号 + 密码）
 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
