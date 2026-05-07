package org.example.ai_educatin.entity.dto.demand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 家长提交需求请求
 */
@Data
public class DemandCreateDTO {

    @NotBlank(message = "孩子年级不能为空")
    private String childGrade;

    @NotBlank(message = "需求类型不能为空")
    private String demandType;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "期望条件不能为空")
    @Size(max = 200, message = "期望条件不超过200字")
    private String expectations;

    /** 预算范围（选填） */
    private String budget;

    /** 补充说明（选填） */
    @Size(max = 500, message = "补充说明不超过500字")
    private String remark;
}
