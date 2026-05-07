package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeachMode {

    OFFLINE(1, "上门"),
    ONLINE(2, "线上"),
    BOTH(3, "均可");

    private final int code;
    private final String desc;
}
