package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 - 家长和老师共用
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名(登录用) */
    private String username;

    /** 密码(BCrypt加密) — 返回给前端时忽略 */
    @JsonIgnore
    private String password;

    /** 微信小程序openid */
    private String openid;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatarUrl;

    /** 角色: 1-家长 2-老师 3-管理员 */
    private Integer role;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
