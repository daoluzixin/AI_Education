package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举（ticket.status）
 */
@Getter
@AllArgsConstructor
public enum TicketStatus {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    RESOLVED(2, "已处理"),
    CLOSED(3, "已关闭");

    private final int code;
    private final String desc;

    public static TicketStatus fromCode(int code) {
        for (TicketStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的工单状态编码: " + code);
    }
}
