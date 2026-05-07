package org.example.ai_educatin.entity.dto.student;

import lombok.Data;

/**
 * 学生补充材料请求
 */
@Data
public class SupplementDTO {

    /** 补充材料URL（逗号分隔） */
    private String supplements;

    /** 学生证照片（重新上传） */
    private String studentIdPhoto;

    /** 获奖证书 */
    private String certificates;

    /** 成绩证明 */
    private String transcripts;
}
