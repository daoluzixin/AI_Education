package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    PARENT(1, "家长"),
    TEACHER(2, "老师"),
    ADMIN(3, "管理员");

    private final int code;
    private final String desc;
}
