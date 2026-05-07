package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;
}
