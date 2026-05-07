package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单咨询类型枚举
 */
@Getter
@AllArgsConstructor
public enum TicketType {

    SERVICE_CONSULT("SERVICE_CONSULT", "服务咨询"),
    MATCH_FEEDBACK("MATCH_FEEDBACK", "匹配反馈"),
    RECOMMEND_UNSATISFIED("RECOMMEND_UNSATISFIED", "推荐不满意"),
    SERVICE_ISSUE("SERVICE_ISSUE", "服务问题"),
    COMPLAINT("COMPLAINT", "投诉建议"),
    REGISTRATION_ISSUE("REGISTRATION_ISSUE", "报名问题"),
    REVIEW_ISSUE("REVIEW_ISSUE", "审核问题"),
    PROFILE_MODIFY("PROFILE_MODIFY", "资料修改"),
    REFUND("REFUND", "退款"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
