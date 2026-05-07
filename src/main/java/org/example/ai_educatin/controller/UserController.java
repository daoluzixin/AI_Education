package org.example.ai_educatin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.user.LoginDTO;
import org.example.ai_educatin.dto.user.RegisterDTO;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户名 + 密码登录
     */
    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 注册新用户
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 根据openid查询用户（微信登录后调用）
     */
    @GetMapping("/openid/{openid}")
    public Result<User> getByOpenid(@PathVariable String openid) {
        return Result.success(userService.getByOpenid(openid));
    }
}
