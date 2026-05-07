package org.example.ai_educatin.entity.dto.student;

import lombok.Data;

/**
 * 学生列表查询条件（后台管理端）
 */
@Data
public class StudentQueryDTO {

    /** 审核状态 */
    private Integer reviewStatus;

    /** 城市 */
    private String city;

    /** 学校 */
    private String schoolName;

    /** 擅长科目（逗号分隔，模糊匹配） */
    private String subjects;

    /** 标签（逗号分隔，模糊匹配） */
    private String tags;

    /** 关键词（姓名/学校/手机号） */
    private String keyword;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
