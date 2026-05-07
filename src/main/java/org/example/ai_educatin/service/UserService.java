package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.user.AdminLoginDTO;
import org.example.ai_educatin.entity.dto.user.ParentLoginDTO;
import org.example.ai_educatin.entity.dto.user.StudentLoginDTO;
import org.example.ai_educatin.entity.User;

public interface UserService extends IService<User> {

    /**
     * 家长登录/注册（手机号 + 验证码，首次自动注册）
     */
    User parentLogin(ParentLoginDTO dto);

    /**
     * 学生登录/注册（手机号 + 教育邮箱 + 验证码，首次自动注册）
     */
    User studentLogin(StudentLoginDTO dto);

    /**
     * 管理员登录（账号 + 密码）
     */
    User adminLogin(AdminLoginDTO dto);

    /**
     * 发送验证码
     */
    void sendVerifyCode(String phone);
}
