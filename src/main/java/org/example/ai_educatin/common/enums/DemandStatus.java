package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求状态枚举（demand.status）
 */
@Getter
@AllArgsConstructor
public enum DemandStatus {

    PENDING(0, "待处理"),
    MATCHING(1, "推荐中"),
    RECOMMENDED(2, "已推荐"),
    CLOSED(3, "已关闭");

    private final int code;
    private final String desc;

    public static DemandStatus fromCode(int code) {
        for (DemandStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的需求状态编码: " + code);
    }
}
