package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.dto.user.LoginDTO;
import org.example.ai_educatin.dto.user.RegisterDTO;
import org.example.ai_educatin.entity.User;

public interface UserService extends IService<User> {

    /**
     * 根据openid查找用户
     */
    User getByOpenid(String openid);

    /**
     * 用户名 + 密码登录
     */
    User login(LoginDTO dto);

    /**
     * 注册新用户
     */
    User register(RegisterDTO dto);
}
