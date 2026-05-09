package org.example.ai_educatin.entity.dto.ticket;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

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

    /** 提交时间-起始 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 提交时间-截止 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
