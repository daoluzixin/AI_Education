package org.example.ai_educatin.entity.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 家长登录请求（手机号 + 验证码）
 */
@Data
public class ParentLoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码为6位数字")
    private String verifyCode;

    /** 昵称（首次登录自动注册时使用） */
    private String nickname;
}
