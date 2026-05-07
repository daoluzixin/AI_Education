package org.example.ai_educatin.entity.dto.ticket;

import lombok.Data;

/**
 * 工单列表查询条件（后台）
 */
@Data
public class TicketQueryDTO {

    /** 用户类型: 1-家长 2-学生 */
    private Integer userType;

    /** 工单状态 */
    private Integer status;

    /** 咨询类型 */
    private String ticketType;

    /** 关键词（工单编号/手机号） */
    private String keyword;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
