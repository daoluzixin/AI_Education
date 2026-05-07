package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推荐反馈类型枚举
 */
@Getter
@AllArgsConstructor
public enum FeedbackType {

    VIEW_DETAIL(1, "查看详情"),
    CONSULT(2, "发起咨询"),
    CONFIRM(3, "确认选择"),
    UNSATISFIED(4, "不满意");

    private final int code;
    private final String desc;

    public static FeedbackType fromCode(int code) {
        for (FeedbackType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的反馈类型编码: " + code);
    }
}
