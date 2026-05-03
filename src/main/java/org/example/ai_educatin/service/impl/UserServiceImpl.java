package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.dto.user.LoginDTO;
import org.example.ai_educatin.dto.user.RegisterDTO;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.mapper.UserMapper;
import org.example.ai_educatin.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public User getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    @Override
    public User login(LoginDTO dto) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        return user;
    }

    @Override
    public User register(RegisterDTO dto) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已被注册");
        }

        // 角色校验: 只允许注册家长(1)或老师(2)
        if (dto.getRole() != 1 && dto.getRole() != 2) {
            throw new BusinessException(400, "角色参数不合法");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1);
        save(user);
        return user;
    }
}
