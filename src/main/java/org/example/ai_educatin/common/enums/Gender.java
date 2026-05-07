package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
@AllArgsConstructor
public enum Gender {

    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;
}
