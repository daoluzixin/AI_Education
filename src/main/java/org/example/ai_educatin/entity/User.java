package org.example.ai_educatin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 - 家长/学生/管理员共用，通过 role 区分
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号(登录凭证,唯一) */
    private String phone;

    /** 密码(BCrypt加密,仅管理员使用) */
    @JsonIgnore
    private String password;

    /** 教育邮箱(.edu.cn,仅学生) */
    private String email;

    /** 昵称(家长注册时填写) */
    private String nickname;

    /** 头像地址 */
    private String avatarUrl;

    /** 角色: 1-家长 2-学生 3-管理员 */
    private Integer role;

    /** 账号状态: 0-禁用 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
