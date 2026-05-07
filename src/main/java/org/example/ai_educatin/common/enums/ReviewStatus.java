package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生审核状态枚举（student_profile.review_status）
 */
@Getter
@AllArgsConstructor
public enum ReviewStatus {

    DRAFT(0, "草稿"),
    PENDING_REVIEW(1, "待审核"),
    APPROVED(2, "审核通过"),
    REJECTED(3, "审核驳回"),
    NEED_SUPPLEMENT(4, "待补充材料");

    private final int code;
    private final String desc;

    public static ReviewStatus fromCode(int code) {
        for (ReviewStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的审核状态编码: " + code);
    }
}
