package org.example.ai_educatin.entity.dto.demand;

import lombok.Data;

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

    /** 页码（从1开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
