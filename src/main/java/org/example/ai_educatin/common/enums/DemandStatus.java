package org.example.ai_educatin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DemandStatus {

    PUBLISHED(0, "发布中"),
    MATCHED(1, "已匹配"),
    IN_PROGRESS(2, "上课中"),
    FINISHED(3, "已完成"),
    CLOSED(4, "已关闭");

    private final int code;
    private final String desc;
}
