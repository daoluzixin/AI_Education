package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    PARENT(1, "家长"),
    STUDENT(2, "学生"),
    ADMIN(3, "管理员");

    private final int code;
    private final String desc;

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色编码: " + code);
    }
}
