package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.admin.ReviewDTO;
import org.example.ai_educatin.entity.ReviewRecord;

import java.util.List;

public interface ReviewRecordService extends IService<ReviewRecord> {

    /**
     * 执行审核操作（通过/驳回/要求补充）
     */
    void doReview(Long studentProfileId, Long reviewerId, String reviewerName, ReviewDTO dto);

    /**
     * 查询某学生的审核记录列表
     */
    List<ReviewRecord> listByStudentId(Long studentProfileId);
}
