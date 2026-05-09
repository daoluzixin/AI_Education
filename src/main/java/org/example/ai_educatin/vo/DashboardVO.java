package org.example.ai_educatin.vo;

import lombok.Data;

/**
 * 后台数据概览 VO（需求文档 6.3.7）
 */
@Data
public class DashboardVO {

    /** 今日新增需求数 */
    private Long todayNewDemands;

    /** 待处理需求数 */
    private Long pendingDemands;

    /** 今日新增报名数（学生提交审核） */
    private Long todayNewStudents;

    /** 待审核学生数 */
    private Long pendingReviewStudents;

    /** 待处理工单数 */
    private Long pendingTickets;

    /** 累计注册家长数 */
    private Long totalParents;

    /** 累计入驻学生数（审核通过） */
    private Long totalApprovedStudents;
}
