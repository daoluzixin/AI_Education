package org.example.ai_educatin.entity.dto.demand;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 需求列表查询条件（后台管理端）
 */
@Data
public class DemandQueryDTO {

    /** 需求状态 */
    private Integer status;

    /** 需求类型 */
    private String demandType;

    /** 城市 */
    private String city;

    /** 关键词（需求编号/手机号） */
    private String keyword;

    /** 提交时间-起始 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 提交时间-截止 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 页码（从1开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
