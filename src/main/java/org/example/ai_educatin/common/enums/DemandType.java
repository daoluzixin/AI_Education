package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求类型枚举
 */
@Getter
@AllArgsConstructor
public enum DemandType {

    SUBJECT_TUTOR("SUBJECT_TUTOR", "学科辅导"),
    INTEREST("INTEREST", "兴趣培养"),
    COMPETITION("COMPETITION", "竞赛指导"),
    ADMISSION("ADMISSION", "升学规划"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String desc;
}
