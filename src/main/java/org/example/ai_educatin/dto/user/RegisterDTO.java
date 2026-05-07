package org.example.ai_educatin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度 3~32 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6~64 位")
    private String password;

    /** 角色: 1-家长 2-老师 */
    @NotNull(message = "请选择角色")
    private Integer role;

    /** 昵称(可选) */
    private String nickname;
}
