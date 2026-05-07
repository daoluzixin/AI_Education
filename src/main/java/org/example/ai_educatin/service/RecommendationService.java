package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.admin.RecommendDTO;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;

import java.util.List;

public interface RecommendationService extends IService<Recommendation> {

    /**
     * 后台配置推荐（最多5位学生）
     */
    void configureRecommendation(RecommendDTO dto, Long operatorId);

    /**
     * 查看某需求的推荐学生列表
     */
    List<StudentProfile> getRecommendedStudents(Long demandId);

    /**
     * 删除某需求下某学生的推荐
     */
    void removeRecommendation(Long demandId, Long studentId);
}
