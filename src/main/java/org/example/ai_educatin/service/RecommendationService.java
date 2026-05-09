package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;

import java.util.List;

public interface RecommendationService extends IService<Recommendation> {

    /**
     * 查看某需求的推荐学生列表
     */
    List<StudentProfile> getRecommendedStudents(Long demandId);
}
